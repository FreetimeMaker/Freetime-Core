"""Keep release-facing SDK constants and dependency examples aligned with the catalog."""

import re
import tomllib
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MODULES = ("Core", "Design", "Browser", "Donations", "FreetimeWarn", "Notifications")


class ReleaseVersionTests(unittest.TestCase):
    def setUp(self):
        catalog = tomllib.loads((ROOT / "gradle/libs.versions.toml").read_text())
        self.version = catalog["versions"]["freetime"]

    def test_root_publishing_uses_catalog_and_original_coordinates(self):
        script = (ROOT / "build.gradle.kts").read_text()
        self.assertIn(".orElse(libs.versions.freetime)", script)
        self.assertIn('group = "com.freetime"', script)
        self.assertNotIn("Freetime-Core-KMP", script)
        self.assertIn("https://github.com/FreetimeMaker/Freetime-Core", script)

    def test_tag_versions_are_normalized_before_publication(self):
        script = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn('.map { it.removePrefix("v") }', script)
        self.assertIn("version = releaseVersion.get()", script)
        self.assertIn("releaseVersion.get())", script)

    def test_maven_local_uses_only_the_unified_publication(self):
        script = (ROOT / "build.gradle.kts").read_text(encoding="utf-8")
        self.assertIn("tasks.withType<PublishToMavenLocal>().configureEach", script)
        self.assertIn(
            'if (name != "publishUnifiedPublicationToMavenLocal") enabled = false',
            script,
        )

    def test_platform_sdk_versions_match_catalog(self):
        for target in ("android", "desktop"):
            path = (
                ROOT
                / f"Core/src/{target}Main/kotlin/com/freetime/core/FreetimeCore.{target}.kt"
            )
            with self.subTest(target=target):
                self.assertIn(f'SDK_VERSION = "{self.version}"', path.read_text())

    def test_current_dependency_examples_match_catalog(self):
        for path in [
            ROOT / "README.md",
            *(ROOT / module / "README.md" for module in MODULES),
        ]:
            with self.subTest(path=path):
                coordinates = re.findall(
                    r"com\.freetime:([\w-]+):([\d.]+)", path.read_text()
                )
                self.assertTrue(coordinates, f"No dependency examples in {path}")
                for artifact, version in coordinates:
                    self.assertIn(artifact, MODULES)
                    self.assertEqual(self.version, version)

    def test_modules_keep_unsuffixed_names(self):
        settings = (ROOT / "settings.gradle.kts").read_text()
        for module in (*MODULES, "Sample", "Sample-android"):
            self.assertIn(f'":{module}"', settings)
        for script in [
            ROOT / "build.gradle.kts",
            *(ROOT / module / "build.gradle.kts" for module in (*MODULES, "Sample")),
        ]:
            self.assertNotRegex(script.read_text(), r'project\(":[^\"]+-kmp"\)')
        self.assertIn(
            'implementation(project(":Sample"))',
            (ROOT / "Sample/androidApp/build.gradle.kts").read_text(),
        )

    def test_sample_is_shared_between_android_and_desktop(self):
        script = (ROOT / "Sample/build.gradle.kts").read_text()
        self.assertIn("alias(libs.plugins.kotlin.multiplatform)", script)
        self.assertIn("androidLibrary", script)
        self.assertIn('jvm("desktop")', script)
        shared = ROOT / "Sample/src/commonMain/kotlin/com/freetime/sample/SampleApp.kt"
        self.assertIn("fun SampleApp()", shared.read_text())
        for entry in (
            ROOT
            / "Sample/androidApp/src/main/kotlin/com/freetime/sample/MainActivity.kt",
            ROOT / "Sample/src/desktopMain/kotlin/com/freetime/sample/Main.kt",
        ):
            self.assertIn("SampleApp()", entry.read_text())

    def test_latest_changelog_matches_catalog(self):
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")

        releases = re.findall(r"^## (\d+\.\d+\.\d+) -", changelog, re.MULTILINE)
        self.assertTrue(releases)
        self.assertEqual(self.version, releases[0])

    def test_sdk_identity_test_matches_catalog(self):
        source = (
            ROOT / "Core/src/commonTest/kotlin/com/freetime/core/FreetimeCoreTest.kt"
        ).read_text(encoding="utf-8")
        self.assertIn(
            f'assertEquals("{self.version}", FreetimeCore.SDK_VERSION)', source
        )

    def test_sample_documentation_matches_app_version(self):
        script = (ROOT / "Sample/androidApp/build.gradle.kts").read_text(
            encoding="utf-8"
        )
        code = re.search(r"versionCode\s*=\s*(\d+)", script).group(1)
        documentation = (ROOT / "Sample/README.md").read_text(encoding="utf-8")
        self.assertIn(
            f"The project release version is **{self.version}**", documentation
        )
        self.assertIn(f"version code **{code}**", documentation)

    def test_sample_uses_shared_release_version(self):
        script = (ROOT / "Sample/androidApp/build.gradle.kts").read_text()
        self.assertIn("versionName = project.version.toString()", script)


if __name__ == "__main__":
    unittest.main()
