"""Offline regression tests for public Maven publishing and verification."""

import io
import json
import sys
import tempfile
import unittest
import urllib.error
import zipfile
from pathlib import Path
from unittest import mock

sys.path.insert(0, str(Path(__file__).resolve().parent))
import publish_public_maven as publish
import verify_public_maven as verify

VERSION = "3.1.0"
FILENAME = f"Core-{VERSION}.aar"
POM = b"""<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.freetime</groupId><artifactId>Core</artifactId>
  <version>3.1.0</version><packaging>aar</packaging>
  <name>Freetime Core</name>
</project>
"""


def archive(entries, timestamp=(2024, 1, 1, 0, 0, 0)):
    output = io.BytesIO()
    with zipfile.ZipFile(output, "w") as target:
        for name, contents in entries.items():
            entry = zipfile.ZipInfo(name, timestamp)
            entry.compress_type = zipfile.ZIP_DEFLATED
            target.writestr(entry, contents)
    return output.getvalue()


class PublicMavenTests(unittest.TestCase):
    def setUp(self):
        # Fail closed if a test accidentally reaches an unmocked network path.
        self.network = mock.patch(
            "urllib.request.urlopen",
            side_effect=AssertionError("Real network forbidden"),
        )
        self.network.start()
        self.addCleanup(self.network.stop)
        self.opener = mock.patch(
            "urllib.request.build_opener",
            side_effect=AssertionError("Real uploads forbidden"),
        )
        self.opener.start()
        self.addCleanup(self.opener.stop)
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.staging = Path(temporary.name)
        self.directory = self.staging / "com/freetime/Core" / VERSION
        self.directory.mkdir(parents=True)
        self.pom_path = self.directory / f"Core-{VERSION}.pom"
        self.aar_path = self.directory / FILENAME
        self.pom_path.write_bytes(POM)
        self.contents = {
            "AndroidManifest.xml": b'<manifest package="com.freetime.core"/>',
            "classes.jar": archive({"com/freetime/Core.class": b"\xca\xfe\xba\xbe"}),
            "res/values/strings.xml": b'<resources><string name="app_name">Core</string></resources>',
            "assets/preserved.bin": bytes(range(256)),
        }
        self.aar_path.write_bytes(archive(self.contents))

    def test_prepare_release_embeds_metadata_preserves_contents_and_is_deterministic(
        self,
    ):
        metadata = "META-INF/maven/com.freetime/Core/"
        self.aar_path.write_bytes(
            archive(
                dict(
                    self.contents,
                    **{
                        metadata + "pom.xml": b"stale POM",
                        metadata + "pom.properties": b"stale properties",
                    },
                )
            )
        )
        original = self.aar_path.read_bytes()
        first = publish.prepare_release(self.staging, ["Core"], VERSION)
        self.assertEqual(
            first, publish.prepare_release(self.staging, ["Core"], VERSION)
        )
        self.assertEqual(len(first), 1)
        module, filename, data = first[0]
        self.assertEqual((module, filename), ("Core", FILENAME))
        with zipfile.ZipFile(io.BytesIO(data)) as result:
            self.assertEqual(len(result.namelist()), len(self.contents) + 2)
            for name, contents in self.contents.items():
                self.assertEqual(result.read(name), contents)
            self.assertEqual(result.read(metadata + "pom.xml"), POM)
            self.assertEqual(
                result.read(metadata + "pom.properties"),
                b"groupId=com.freetime\nartifactId=Core\nversion=3.1.0\n",
            )
        self.assertEqual(self.aar_path.read_bytes(), original)
        self.assertEqual(self.pom_path.read_bytes(), POM)

    def test_prepare_release_rejects_incorrect_coordinates_and_packaging(self):
        for old, new in (
            (b"com.freetime", b"org.other"),
            (b">Core<", b">Other<"),
            (b"3.1.0", b"3.0.0"),
            (b">aar<", b">jar<"),
        ):
            with self.subTest(value=new):
                self.pom_path.write_bytes(POM.replace(old, new))
                with self.assertRaises(ValueError):
                    publish.prepare_release(self.staging, ["Core"], VERSION)

    def test_invalid_cli_version_is_rejected_before_preflight(self):
        for script in (publish, verify):
            with (
                self.subTest(script=script.__name__),
                mock.patch.object(sys, "argv", [script.__name__, "../bad"]),
                mock.patch("sys.stderr", new_callable=io.StringIO),
            ):
                with self.assertRaises(SystemExit) as error:
                    script.main()
                self.assertEqual(error.exception.code, 2)

    def test_missing_file_fails_preflight_before_any_upload(self):
        for path in (self.pom_path, self.aar_path):
            with self.subTest(path=path.name):
                original = path.read_bytes()
                path.unlink()
                try:
                    with self.assertRaises(FileNotFoundError):
                        publish.prepare_release(self.staging, ["Core"], VERSION)
                finally:
                    path.write_bytes(original)
        with (
            mock.patch.object(
                sys, "argv", ["publish", VERSION, "--staging", str(self.staging)]
            ),
            mock.patch.object(publish, "MODULES", ("Core", "Missing")),
            mock.patch.object(publish, "upload") as upload,
            mock.patch.object(publish, "read_token") as token,
        ):
            with self.assertRaises(SystemExit):
                publish.main()
            upload.assert_not_called()
            token.assert_not_called()

    def test_verifier_requires_the_exact_release_coordinates(self):
        expected = {
            "groupId": "com.freetime",
            "artifactId": "Core",
            "version": VERSION,
            "packaging": "aar",
        }
        with mock.patch.object(
            verify.urllib.request, "urlopen", return_value=io.BytesIO(POM)
        ):
            verify.verify_artifact("https://example.org/Core.pom", "pom", expected)
        for key in expected:
            with (
                self.subTest(key=key),
                mock.patch.object(
                    verify.urllib.request, "urlopen", return_value=io.BytesIO(POM)
                ),
                self.assertRaises(ValueError),
            ):
                verify.verify_artifact(
                    "https://example.org/Core.pom",
                    "pom",
                    dict(expected, **{key: "wrong"}),
                )

    def test_upload_multipart_and_confirmation(self):
        payload = self.aar_path.read_bytes()
        with mock.patch.object(publish.urllib.request, "build_opener") as build:
            build.return_value.open.return_value = io.BytesIO(
                json.dumps({"package_id": 12, "version_id": 34}).encode()
            )
            self.assertTrue(publish.upload(FILENAME, payload, "secret-test-token"))
            self.assertIsInstance(build.call_args.args[0], publish.NoRedirect)
            request = build.return_value.open.call_args.args[0]
            self.assertEqual(request.method, "POST")
            self.assertEqual(
                request.full_url, "https://push.fury.io/uploads?as=freetimemaker"
            )
            self.assertEqual(request.get_header("Authorization"), "secret-test-token")
            boundary = request.get_header("Content-type").split("boundary=", 1)[1]
            expected = (
                (
                    f'--{boundary}\r\nContent-Disposition: form-data; name="public"\r\n\r\ntrue\r\n'
                    f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{FILENAME}"\r\n'
                    "Content-Type: application/octet-stream\r\n\r\n"
                ).encode()
                + payload
                + f"\r\n--{boundary}--\r\n".encode()
            )
            self.assertEqual(request.data, expected)
            self.assertEqual(build.return_value.open.call_args.kwargs, {"timeout": 180})

    def test_upload_requires_both_response_ids(self):
        for response in (
            {},
            {"package_id": 1},
            {"version_id": 2},
            {"package_id": 1, "version_id": None},
            [],
            None,
        ):
            with (
                self.subTest(response=response),
                mock.patch.object(publish.urllib.request, "build_opener") as build,
            ):
                build.return_value.open.return_value = io.BytesIO(
                    json.dumps(response).encode()
                )
                with self.assertRaises(ValueError):
                    publish.upload(FILENAME, b"archive", "secret-test-token")

    def test_upload_conflict_and_unauthorized(self):
        for status in (409, 401):
            with (
                self.subTest(status=status),
                mock.patch.object(publish.urllib.request, "build_opener") as build,
            ):
                http_error = urllib.error.HTTPError(
                    "https://push.fury.io/uploads",
                    status,
                    "secret-test-token",
                    {},
                    None,
                )
                self.addCleanup(http_error.close)
                build.return_value.open.side_effect = http_error
                if status == 409:
                    self.assertFalse(
                        publish.upload(FILENAME, b"archive", "secret-test-token")
                    )
                else:
                    with self.assertRaises(ValueError) as error:
                        publish.upload(FILENAME, b"archive", "secret-test-token")
                    self.assertIn("401", str(error.exception))
                    self.assertNotIn("secret-test-token", str(error.exception))
                self.assertEqual(build.return_value.open.call_count, 1)

    def test_matching_public_release_compares_contents_not_zip_timestamps(self):
        local = archive(self.contents)
        remote = archive(self.contents, (2025, 6, 2, 12, 30, 0))
        self.assertNotEqual(local, remote)
        changed = archive(dict(self.contents, **{"classes.jar": b"different"}))
        for data, expected in ((remote, True), (changed, False)):
            with (
                self.subTest(expected=expected),
                mock.patch.object(
                    publish.urllib.request, "urlopen", return_value=io.BytesIO(data)
                ) as get,
            ):
                self.assertEqual(
                    publish.matching_public_release("Core", FILENAME, local, VERSION),
                    expected,
                )
                get.assert_called_once_with(
                    f"{publish.REPOSITORY}com/freetime/Core/{VERSION}/{FILENAME}",
                    timeout=30,
                )
        http_error = urllib.error.HTTPError(
            "https://example.invalid", 404, "Not Found", {}, None
        )
        self.addCleanup(http_error.close)
        with mock.patch.object(
            publish.urllib.request, "urlopen", side_effect=http_error
        ):
            self.assertFalse(
                publish.matching_public_release("Core", FILENAME, local, VERSION)
            )

    def test_verifier_rejects_html_instead_of_pom_or_aar(self):
        for extension in ("pom", "aar"):
            with (
                self.subTest(extension=extension),
                mock.patch.object(
                    verify.urllib.request,
                    "urlopen",
                    return_value=io.BytesIO(
                        b"<html><body>Login required</body></html>"
                    ),
                ),
            ):
                with self.assertRaises(ValueError):
                    verify.verify_artifact(
                        f"https://example.invalid/Core.{extension}", extension
                    )


if __name__ == "__main__":
    unittest.main()
