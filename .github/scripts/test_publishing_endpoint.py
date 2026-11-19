"""Keep deployment separate from the public download endpoint."""

import unittest
from pathlib import Path


class PublishingEndpointTests(unittest.TestCase):
    def test_native_maven_deployment_endpoint(self):
        root = Path(__file__).resolve().parents[2]
        build = (root / "build.gradle.kts").read_text()
        self.assertIn('url = uri("https://maven.fury.io/freetimemaker/")', build)
        self.assertNotIn('url = uri("https://repo.free-time.me/maven/")', build)


if __name__ == "__main__":
    unittest.main()
