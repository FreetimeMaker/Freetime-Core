"""Guard against re-enabling remote publishing."""

import unittest
from pathlib import Path


class PublishingEndpointTests(unittest.TestCase):
    def test_publishing_repositories_are_local(self):
        root = Path(__file__).resolve().parents[2]
        build = (root / "build.gradle.kts").read_text()
        urls = [
            line.strip()
            for line in build.splitlines()
            if line.strip().startswith("url =")
        ]
        self.assertEqual(
            urls,
            [
                'url = rootProject.layout.buildDirectory.dir("native-maven").get().asFile.toURI()',
                'url = rootProject.layout.buildDirectory.dir("public-maven").get().asFile.toURI()',
            ],
        )


if __name__ == "__main__":
    unittest.main()
