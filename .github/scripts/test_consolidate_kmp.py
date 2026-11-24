"""Offline integration tests using native-shaped Gradle metadata and ZIPs."""

import ast
import copy
import hashlib
import io
import json
import tempfile
import unittest
import zipfile
from pathlib import Path

from consolidate_kmp_publications import (
    MODULES,
    _inside,
    _resolve,
    _without_suffix,
    consolidate,
)

VERSION = "4.0.1"


class ConsolidationTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.staging = self.base / "native"
        self.output = self.base / "unified"
        for module in MODULES:
            self.fixture(module)

    def directory(self, module):
        return self.staging / "com/freetime" / module / VERSION

    def metadata(self, module):
        return self.directory(module) / f"{module}-{VERSION}.module"

    def write_metadata(self, module, data):
        path = self.metadata(module)
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(data))

    def alter(self, module, change):
        data = json.loads(self.metadata(module).read_text())
        change(data)
        self.write_metadata(module, data)

    def fixture(self, module):
        root = {
            "formatVersion": "1.1",
            "component": {
                "group": "com.freetime",
                "module": module,
                "version": VERSION,
                "attributes": {"org.gradle.status": "release"},
            },
            "createdBy": {"gradle": {"version": "9.0"}},
            "variants": [],
        }
        for platform, kotlin_type in (
            (None, "common"),
            ("android", "androidJvm"),
            ("desktop", "jvm"),
        ):
            coordinate = module + ("-" + platform if platform else "")
            directory = self.directory(coordinate)
            directory.mkdir(parents=True, exist_ok=True)
            target = copy.deepcopy(root)
            target["variants"] = []
            if platform:
                target["component"]["url"] = (
                    f"../../{module}/{VERSION}/{module}-{VERSION}.module"
                )
            for kind in (
                ("Api", "Sources") if not platform else ("Api", "Runtime", "Sources")
            ):
                sources = kind == "Sources"
                attrs = {
                    "org.gradle.category": "documentation" if sources else "library",
                    "org.jetbrains.kotlin.platform.type": kotlin_type,
                    "org.gradle.usage": "kotlin-runtime"
                    if sources and not platform
                    else "kotlin-metadata"
                    if not platform
                    else "java-api"
                    if kind == "Api"
                    else "java-runtime",
                }
                if sources:
                    attrs["org.gradle.docstype"] = "sources"
                name = f"{platform or 'metadata'}{kind}Elements"
                extension = "aar" if platform == "android" and not sources else "jar"
                filename = (
                    f"{coordinate}-{VERSION}{'-sources' if sources else ''}.{extension}"
                )
                buffer = io.BytesIO()
                with zipfile.ZipFile(buffer, "w") as archive:
                    archive.writestr("content.txt", f"{module}:{platform}:{sources}")
                payload = buffer.getvalue()
                (directory / filename).write_bytes(payload)
                file = {
                    "name": "native-display-name." + extension,
                    "url": filename,
                    "size": len(payload),
                }
                file.update(
                    {
                        h: hashlib.new(h, payload).hexdigest()
                        for h in ("sha512", "sha256", "sha1", "md5")
                    }
                )
                variant = {"name": name, "attributes": attrs, "files": [file]}
                if not sources:
                    variant["dependencies"] = [
                        {
                            "group": "com.freetime",
                            "module": "Core" + ("-" + platform if platform else ""),
                            "version": {"requires": VERSION},
                        },
                        {
                            "group": "third.party",
                            "module": "Core-android",
                            "version": {"strictly": "2.0", "rejects": ["2.1"]},
                            "excludes": [{"group": "x", "module": "y"}],
                        },
                        {
                            "group": "com.freetime",
                            "module": "Unknown-desktop",
                            "version": {"requires": "1.0"},
                        },
                    ]
                    if kind == "Runtime":
                        variant["dependencies"].append(
                            {
                                "group": "runtime.only",
                                "module": "extra",
                                "version": {"requires": "3"},
                            }
                        )
                    variant["dependencyConstraints"] = [
                        {
                            "group": "com.freetime",
                            "module": "Design-desktop",
                            "version": {"requires": VERSION},
                        }
                    ]
                if platform:
                    target["variants"].append(variant)
                    root["variants"].append(
                        {
                            "name": name + "-published",
                            "attributes": attrs,
                            "available-at": {
                                "group": "com.freetime",
                                "module": coordinate,
                                "version": VERSION,
                                "url": f"../../{coordinate}/{VERSION}/{coordinate}-{VERSION}.module",
                            },
                        }
                    )
                else:
                    root["variants"].append(variant)
            if platform:
                self.write_metadata(coordinate, target)
        self.write_metadata(module, root)
        (
            self.directory(module) / "unreferenced-kotlin-tooling-metadata.json"
        ).write_text("{}")

    def run_consolidate(self):
        return consolidate(self.staging, self.output, VERSION)

    def assert_invalid(self, exception=ValueError):
        with self.assertRaises(exception):
            self.run_consolidate()
        self.assertFalse(self.output.exists())

    def test_complete_tree_hashes_dependencies_and_source_unchanged(self):
        before = {
            p.relative_to(self.staging): p.read_bytes()
            for p in self.staging.rglob("*")
            if p.is_file()
        }
        paths = self.run_consolidate()
        self.assertEqual(len(paths), 42)
        self.assertEqual(set(paths), {p for p in self.output.rglob("*") if p.is_file()})
        for module in MODULES:
            directory = self.output / "com/freetime" / module / VERSION
            data = json.loads((directory / f"{module}-{VERSION}.module").read_text())
            self.assertEqual(data["component"]["module"], module)
            self.assertEqual(data["component"]["version"], VERSION)
            self.assertEqual(len(data["variants"]), 8)
            self.assertNotIn("available-at", json.dumps(data))
            urls = set()
            for variant in data["variants"]:
                for file in variant["files"]:
                    urls.add(file["url"])
                    payload = (directory / file["url"]).read_bytes()
                    self.assertEqual(file["name"], file["url"])
                    self.assertEqual(file["size"], len(payload))
                    for h in ("md5", "sha1", "sha256", "sha512"):
                        self.assertEqual(file[h], hashlib.new(h, payload).hexdigest())
                    with zipfile.ZipFile(io.BytesIO(payload)) as archive:
                        self.assertIn("content.txt", archive.namelist())
                if "dependencies" in variant:
                    self.assertEqual(variant["dependencies"][0]["module"], "Core")
                    self.assertEqual(
                        variant["dependencies"][1],
                        {
                            "group": "third.party",
                            "module": "Core-android",
                            "version": {"strictly": "2.0", "rejects": ["2.1"]},
                            "excludes": [{"group": "x", "module": "y"}],
                        },
                    )
                    self.assertEqual(
                        variant["dependencies"][2]["module"], "Unknown-desktop"
                    )
                    self.assertEqual(
                        variant["dependencyConstraints"][0]["module"], "Design"
                    )
                    if "Runtime" in variant["name"]:
                        self.assertEqual(
                            variant["dependencies"][-1]["group"], "runtime.only"
                        )
            self.assertEqual(len(urls), 6)
            for suffix in (
                "-sources.jar",
                "-android-sources.jar",
                "-desktop-sources.jar",
            ):
                self.assertIn(f"{module}-{VERSION}{suffix}", urls)
        self.assertEqual(
            before,
            {
                p.relative_to(self.staging): p.read_bytes()
                for p in self.staging.rglob("*")
                if p.is_file()
            },
        )
        self.assertEqual(paths, self.run_consolidate())

    def test_runtime_avoids_newer_python_syntax_and_path_apis(self):
        # JitPack can run this one production script with its legacy system python3.
        source = (
            Path(__file__)
            .with_name("consolidate_kmp_publications.py")
            .read_text(encoding="utf-8")
        )
        tree = ast.parse(source)
        for node in ast.walk(tree):
            self.assertNotIsInstance(
                node, (ast.JoinedStr, ast.AnnAssign, ast.NamedExpr)
            )
            if isinstance(node, ast.Attribute):
                self.assertNotIn(
                    node.attr, ("is_relative_to", "removeprefix", "removesuffix")
                )

    def test_path_helpers_preserve_boundary_checks_for_missing_outputs(self):
        boundary = _resolve(self.base / "not-created")
        self.assertTrue(_inside(_resolve(boundary / "child"), boundary))
        self.assertTrue(_inside(boundary, boundary))
        self.assertFalse(_inside(_resolve(self.base / "not-created-sibling"), boundary))
        self.assertFalse(_inside(_resolve(boundary / ".." / "outside"), boundary))
        self.assertFalse(boundary.exists())

    def test_suffix_helper_removes_only_the_exact_suffix(self):
        self.assertEqual("api", _without_suffix("api-published", "-published"))
        self.assertEqual(
            "published-api", _without_suffix("published-api", "-published")
        )
        self.assertEqual("api-publishe", _without_suffix("api-publishe", "-published"))

    def test_missing_target_metadata(self):
        self.metadata("Notifications-desktop").unlink()
        self.assert_invalid(FileNotFoundError)

    def test_missing_referenced_file(self):
        (
            self.directory("Notifications-desktop")
            / f"Notifications-desktop-{VERSION}-sources.jar"
        ).unlink()
        self.assert_invalid(FileNotFoundError)

    def test_bad_checksums_and_size(self):
        original = self.metadata("Core").read_text()
        for field, value in (("sha256", "bad"), ("md5", "bad"), ("size", -1)):
            with self.subTest(field=field):
                self.metadata("Core").write_text(original)
                self.alter(
                    "Core",
                    lambda d: d["variants"][0]["files"][0].update({field: value}),
                )
                self.assert_invalid()

    def test_invalid_semver(self):
        for version in (
            "4.01.0",
            "v4.0.1",
            "4.0",
            "4.0.1-01",
            "../4.0.1",
            "4.0.1\n",
            "4.0.1+",
        ):
            with self.subTest(version=version), self.assertRaises(ValueError):
                consolidate(self.staging, self.output, version)
        self.assertFalse(self.output.exists())

    def test_version_mismatch(self):
        self.alter("Core-android", lambda d: d["component"].update(version="4.0.0"))
        self.assert_invalid()

    def test_unknown_variant(self):
        self.alter(
            "Core-desktop",
            lambda d: d["variants"][0]["attributes"].update(
                {"org.gradle.usage": "unknown"}
            ),
        )
        self.assert_invalid()

    def test_attribute_fallback(self):
        self.alter("Core-desktop", lambda d: d["variants"][0].update(name="renamedApi"))
        self.assertEqual(len(self.run_consolidate()), 42)

    def test_missing_sources_variant(self):
        self.alter("Core", lambda d: d["variants"].pop(1))
        self.assert_invalid()

    def test_missing_runtime_variant(self):
        self.alter(
            "Core",
            lambda d: d["variants"].__setitem__(
                slice(None),
                [
                    v
                    for v in d["variants"]
                    if v["name"] != "desktopRuntimeElements-published"
                ],
            ),
        )
        self.assert_invalid()

    def test_unsafe_urls(self):
        original = self.metadata("Core").read_text()
        for url in (
            "https://example.com/file.jar",
            "../../../../../../outside.jar",
            "/etc/passwd",
            "file:///etc/passwd",
            "%2e%2e/out.jar",
        ):
            with self.subTest(url=url):
                self.metadata("Core").write_text(original)
                self.alter(
                    "Core", lambda d: d["variants"][0]["files"][0].update(url=url)
                )
                self.assert_invalid()

    def test_symlink_escape(self):
        file = self.directory("Core") / f"Core-{VERSION}.jar"
        outside = self.base / "outside.jar"
        outside.write_bytes(file.read_bytes())
        file.unlink()
        file.symlink_to(outside)
        self.assert_invalid()

    def test_disjoint_paths(self):
        with self.assertRaises(ValueError):
            consolidate(self.staging, self.staging / "output", VERSION)

    def test_existing_output_unchanged_on_failure(self):
        self.output.mkdir()
        marker = self.output / "marker"
        marker.write_text("keep")
        self.metadata("Notifications-desktop").unlink()
        with self.assertRaises(FileNotFoundError):
            self.run_consolidate()
        self.assertEqual(list(self.output.iterdir()), [marker])
        self.assertEqual(marker.read_text(), "keep")


if __name__ == "__main__":
    unittest.main()
