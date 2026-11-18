import contextlib
import io
import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import verify_gemfury_release as verify


class GemfuryReleaseTests(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.staging = Path(self.directory.name)
        for module in verify.MODULES:
            for target in ("", "-android", "-desktop"):
                artifact = f"{module}{target}"
                directory = self.staging / "com/freetime" / artifact / "1.0.0"
                directory.mkdir(parents=True)
                for extension in (
                    "pom",
                    "module",
                    "aar" if target == "-android" else "jar",
                ):
                    (directory / f"{artifact}-1.0.0.{extension}").write_bytes(
                        b"release-content"
                    )

    def test_requires_all_eighteen_publications(self):
        self.assertEqual(54, len(verify.release_files(self.staging, "1.0.0")))
        next(self.staging.rglob("*.module")).unlink()
        with self.assertRaises(ValueError):
            verify.release_files(self.staging, "1.0.0")

    def test_checks_anonymous_downloads(self):
        with patch.object(
            verify.urllib.request,
            "urlopen",
            side_effect=lambda *a, **k: io.BytesIO(b"release-content"),
        ) as request:
            with contextlib.redirect_stdout(io.StringIO()):
                verify.verify_release(self.staging, "1.0.0", attempts=1)
            self.assertEqual(54, request.call_count)
            self.assertTrue(request.call_args.args[0].startswith(verify.REPOSITORY))

    def test_includes_classifiers_and_other_staged_artifacts(self):
        directory = self.staging / "com/freetime/Core/1.0.0"
        for filename in ("Core-1.0.0-sources.jar", "Core-1.0.0-extra.zip"):
            (directory / filename).write_bytes(b"release-content")
        (directory / "Core-1.0.0.pom.sha256").write_text("checksum")
        files = verify.release_files(self.staging, "1.0.0")
        self.assertEqual(56, len(files))
        with patch.object(
            verify.urllib.request, "urlopen", side_effect=lambda *a, **k: io.BytesIO(b"release-content")
        ) as request:
            with contextlib.redirect_stdout(io.StringIO()):
                verify.verify_release(self.staging, "1.0.0", attempts=1)
        self.assertEqual(len(files), request.call_count)

    def test_rejects_invalid_version_without_network(self):
        with patch.object(verify.urllib.request, "urlopen") as request:
            with self.assertRaises(ValueError):
                verify.verify_release(self.staging, "../1.0.0", attempts=1)
            request.assert_not_called()

    def test_missing_metadata_cannot_be_certified_by_successful_upload(self):
        next(self.staging.rglob("*.module")).unlink()
        with patch.object(verify.urllib.request, "urlopen") as request:
            with self.assertRaises(ValueError):
                verify.verify_release(self.staging, "1.0.0", attempts=1)
            request.assert_not_called()

    def test_rejects_changed_or_error_page_content(self):
        with patch.object(
            verify.urllib.request, "urlopen", return_value=io.BytesIO(b"login-page")
        ):
            with self.assertRaises(ValueError):
                verify.verify_release(self.staging, "1.0.0", attempts=1)


if __name__ == "__main__":
    unittest.main()
