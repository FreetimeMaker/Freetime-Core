"""Regression tests: legacy commands must never upload or certify KMP releases."""

import subprocess
import sys
import unittest
from pathlib import Path
from unittest import mock

import publish_public_maven as publish
import verify_public_maven as verify


class LegacyPublishingTests(unittest.TestCase):
    def test_publisher_fails_without_network(self):
        with mock.patch("urllib.request.urlopen") as request:
            with self.assertRaises(SystemExit) as error:
                publish.main()
            request.assert_not_called()
        self.assertIn(".module", str(error.exception))
        self.assertIn("stagePublications", str(error.exception))
        self.assertIn("Remote publishing is disabled", str(error.exception))

    def test_verifier_cannot_certify_aar_only_release(self):
        with self.assertRaises(SystemExit) as error:
            verify.main()
        self.assertIn(".module", str(error.exception))

    def test_legacy_cli_always_exits_unsuccessfully(self):
        for script in ("publish_public_maven.py", "verify_public_maven.py"):
            for args in ([], ["1.0.0"], ["1.0.0", "--dry-run"]):
                with self.subTest(script=script, args=args):
                    result = subprocess.run(
                        [sys.executable, str(Path(__file__).with_name(script)), *args],
                        capture_output=True,
                        text=True,
                        timeout=10,
                    )
                    self.assertNotEqual(result.returncode, 0)
                    self.assertIn(".module", result.stderr)


if __name__ == "__main__":
    unittest.main()
