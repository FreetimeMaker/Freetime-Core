"""Require anonymous access to every published Freetime Maven artifact."""

import argparse
import json
import re
import time
import urllib.error
import urllib.request
import xml.etree.ElementTree as ET

REPOSITORY = "https://repo.free-time.me/maven/"
MODULES = ("Core", "Design", "Browser", "Donations", "FreetimeWarn")


def verify_artifact(url, extension):
    # No credentials or authentication handlers: public access must work on its own.
    with urllib.request.urlopen(url, timeout=30) as response:
        if extension == "pom":
            root = ET.fromstring(response.read())
            if root.tag != "{http://maven.apache.org/POM/4.0.0}project":
                raise ValueError("Expected a Maven POM, not a login or error page")
        elif extension == "module":
            metadata = json.load(response)
            if metadata.get("component", {}).get("group") != "com.freetime":
                raise ValueError("Expected Freetime Gradle module metadata")
        elif response.read(4) != b"PK\x03\x04":
            raise ValueError(
                "Expected a ZIP-based Maven artifact, not a login or error page"
            )


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version")
    args = parser.parse_args()
    if not re.fullmatch(r"[0-9]+\.[0-9]+\.[0-9]+(?:[.-][0-9A-Za-z.-]+)?", args.version):
        parser.error("Expected a semantic release version")

    for module in MODULES:
        base = (
            f"{REPOSITORY}com/freetime/{module}/{args.version}/{module}-{args.version}"
        )
        for suffix, extension in (
            (".pom", "pom"),
            (".module", "module"),
            (".aar", "aar"),
            ("-sources.jar", "jar"),
            ("-javadoc.jar", "jar"),
        ):
            url = base + suffix
            for attempt in range(3):
                try:
                    verify_artifact(url, extension)
                    break
                except (OSError, ValueError, ET.ParseError) as error:
                    if attempt == 2:
                        raise SystemExit(
                            f"Public download check failed: {url}\n{error}\n"
                            "In Gemfury, open this package's settings and select "
                            "'Make package public'. Also check the custom repository's routing. "
                            "Do not create the release tag until anonymous access works."
                        ) from error
                    time.sleep(5)
            print(f"Public download verified: {url}")


if __name__ == "__main__":
    main()
