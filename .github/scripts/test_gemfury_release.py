import contextlib
import io
import json
import tempfile
import unittest
import urllib.error
from pathlib import Path
from unittest.mock import patch

import verify_gemfury_release as verify


class GemfuryReleaseTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.staging = Path(self.directory.name)
        for module in verify.MODULES:
            directory = self.staging / "com/freetime" / module / "1.0.0"
            directory.mkdir(parents=True)
            prefix = f"{module}-1.0.0"
            variants = []
            for platform, classifier, extension in (
                ("common", "", ".jar"),
                ("androidJvm", "-android", ".aar"),
                ("jvm", "-desktop", ".jar"),
            ):
                for usage in (
                    ("kotlin-metadata",)
                    if platform == "common"
                    else ("java-api", "java-runtime")
                ):
                    variants.append(
                        {
                            "name": platform + usage,
                            "attributes": {
                                "org.jetbrains.kotlin.platform.type": platform,
                                "org.gradle.usage": usage,
                                "org.gradle.category": "library",
                            },
                            "files": [
                                {
                                    "name": prefix + classifier + extension,
                                    "url": prefix + classifier + extension,
                                }
                            ],
                        }
                    )
                source = prefix + classifier + "-sources.jar"
                variants.append(
                    {
                        "name": platform + "Sources",
                        "attributes": {
                            "org.jetbrains.kotlin.platform.type": platform,
                            "org.gradle.usage": "kotlin-runtime",
                            "org.gradle.category": "documentation",
                        },
                        "files": [{"name": source, "url": source}],
                    }
                )
            # Native source variants use runtime usage too, but are documentation.
            for variant in variants:
                if variant["name"].endswith("Sources"):
                    variant["attributes"]["org.gradle.usage"] = "java-runtime"
            metadata = {
                "formatVersion": "1.1",
                "component": {
                    "group": "com.freetime",
                    "module": module,
                    "version": "1.0.0",
                },
                "variants": variants,
            }
            for suffix in (
                ".pom",
                ".jar",
                "-android.aar",
                "-desktop.jar",
                "-sources.jar",
                "-android-sources.jar",
                "-desktop-sources.jar",
            ):
                (directory / (prefix + suffix)).write_bytes(b"release-content")
            (directory / (prefix + ".module")).write_text(json.dumps(metadata))
        self.metadata = self.staging / "com/freetime/Core/1.0.0/Core-1.0.0.module"

    def mutate(self, change):
        data = json.loads(self.metadata.read_text())
        change(data)
        self.metadata.write_text(json.dumps(data))

    def assert_local_failure(self, message):
        with patch.object(verify.urllib.request, "urlopen") as request:
            with self.assertRaisesRegex(ValueError, message):
                verify.verify_release(self.staging, "1.0.0", attempts=1)
            request.assert_not_called()

    def test_six_root_publications(self):
        files = verify.release_files(self.staging, "1.0.0")
        self.assertEqual(48, len(files))
        self.assertEqual(set(verify.MODULES), {p.parts[2] for p in files})

    def test_anonymous_exact_downloads_optional_extras_ignored(self):
        self.metadata.with_name("Core-1.0.0-tooling-metadata.json").write_text("{}")
        self.metadata.with_name("Core-1.0.0-extra.zip").write_bytes(b"extra")
        files = verify.release_files(self.staging, "1.0.0")

        def download(url, timeout):
            return io.BytesIO(
                (self.staging / url.removeprefix(verify.REPOSITORY)).read_bytes()
            )

        with patch.object(
            verify.urllib.request, "urlopen", side_effect=download
        ) as request:
            with contextlib.redirect_stdout(io.StringIO()):
                verify.verify_release(self.staging, "1.0.0", attempts=1)
            self.assertEqual(48, request.call_count)
            self.assertEqual(
                set(files),
                {
                    Path(c.args[0].removeprefix(verify.REPOSITORY))
                    for c in request.call_args_list
                },
            )

    def test_required_files(self):
        for suffix, message in (
            (".module", "module metadata"),
            (".jar", "main artifact"),
            ("-android.aar", "main artifact"),
            ("-desktop.jar", "main artifact"),
            ("-sources.jar", "metadata-referenced"),
        ):
            with self.subTest(suffix=suffix):
                path = self.metadata.with_name("Core-1.0.0" + suffix)
                contents = path.read_bytes()
                path.unlink()
                self.assert_local_failure(message)
                path.write_bytes(contents)

    def test_invalid_json(self):
        self.metadata.write_bytes(b"not JSON")
        self.assert_local_failure("Invalid module JSON")

    def test_format_version(self):
        original = self.metadata.read_text()
        for version in (None, "1.0", 1.1):
            with self.subTest(version=version):
                self.metadata.write_text(original)
                self.mutate(lambda d: d.update(formatVersion=version))
                self.assert_local_failure("Invalid module formatVersion")
        self.metadata.write_text(original)
        self.mutate(lambda d: d.pop("formatVersion"))
        self.assert_local_failure("Invalid module formatVersion")

    def test_metadata_usage_does_not_replace_jvm_api(self):
        self.mutate(
            lambda d: next(v for v in d["variants"] if v["name"] == "jvmjava-api")[
                "attributes"
            ].update({"org.gradle.usage": "kotlin-metadata"})
        )
        self.assert_local_failure("Missing target")

    def test_common_metadata_requires_main_reference(self):
        self.mutate(
            lambda d: d["variants"][0]["files"][0].update(url="Core-1.0.0-sources.jar")
        )
        self.assert_local_failure("Missing common main artifact reference")

    def test_component_mismatch(self):
        self.mutate(lambda d: d["component"].update(version="4.0.0"))
        self.assert_local_failure("Invalid module component")

    def test_unsafe_urls(self):
        original = self.metadata.read_text()
        for url in (
            "../Core.jar",
            "https://example.com/Core.jar",
            "/Core.jar",
            "Core%2f.jar",
            "../../Core-android/1.0.0/Core.jar",
            "Core-1.0.0.jar?x=1",
        ):
            with self.subTest(url=url):
                self.metadata.write_text(original)
                self.mutate(lambda d: d["variants"][0]["files"][0].update(url=url))
                self.assert_local_failure("Unsafe")

    def test_redirect_rejected(self):
        self.mutate(
            lambda d: d["variants"][0].update(
                {"available-at": {"url": "../Core-android/module"}}
            )
        )
        self.assert_local_failure("available-at")

    def test_target_presence(self):
        self.mutate(
            lambda d: d.update(
                variants=[
                    v
                    for v in d["variants"]
                    if v["attributes"]["org.jetbrains.kotlin.platform.type"] != "jvm"
                ]
            )
        )
        self.assert_local_failure("Missing target")

    def test_runtime_presence(self):
        self.mutate(
            lambda d: d.update(
                variants=[v for v in d["variants"] if v["name"] != "jvmjava-runtime"]
            )
        )
        self.assert_local_failure("Missing target")

    def test_platform_type(self):
        self.mutate(
            lambda d: d["variants"][0]["attributes"].update(
                {"org.jetbrains.kotlin.platform.type": 123}
            )
        )
        self.assert_local_failure("platform/usage")

    def test_bad_version(self):
        with patch.object(verify.urllib.request, "urlopen") as request:
            with self.assertRaisesRegex(ValueError, "Invalid release version"):
                verify.verify_release(self.staging, "../1.0.0")
            request.assert_not_called()

    def test_bad_remote_content(self):
        for content in (b"login-page", b"changed-artifact"):
            with patch.object(
                verify.urllib.request, "urlopen", return_value=io.BytesIO(content)
            ):
                with self.assertRaisesRegex(ValueError, "Published content differs"):
                    verify.verify_release(self.staging, "1.0.0", attempts=1)

    def test_http_error_preserves_status_and_type(self):
        def download(url, timeout):
            if url.endswith(".module"):
                error = urllib.error.HTTPError(url, 404, "Not Found", {}, None)
                self.addCleanup(error.close)
                raise error
            return io.BytesIO(
                (self.staging / url.removeprefix(verify.REPOSITORY)).read_bytes()
            )

        with patch.object(verify.urllib.request, "urlopen", side_effect=download):
            with contextlib.redirect_stdout(io.StringIO()):
                with self.assertRaisesRegex(
                    ValueError, "module metadata.*HTTP Error 404: Not Found"
                ):
                    verify.verify_release(self.staging, "1.0.0", attempts=1)

    def test_local_only_cli(self):
        with (
            patch(
                "sys.argv",
                ["verify", "1.0.0", "--staging", str(self.staging), "--local-only"],
            ),
            patch.object(verify.urllib.request, "urlopen") as request,
        ):
            with contextlib.redirect_stdout(io.StringIO()):
                verify.main()
            request.assert_not_called()

    def test_cli_error_no_traceback(self):
        with (
            patch("sys.argv", ["verify", "../bad"]),
            contextlib.redirect_stderr(io.StringIO()) as stderr,
        ):
            with self.assertRaises(SystemExit) as error:
                verify.main()
            self.assertEqual(2, error.exception.code)
            self.assertIn("Invalid release version", stderr.getvalue())
            self.assertNotIn("Traceback", stderr.getvalue())


if __name__ == "__main__":
    unittest.main()
