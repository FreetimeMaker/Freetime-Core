"""Fail closed for the retired Gemfury archive upload path."""

MESSAGE = (
    "The legacy archive uploader is retired. Use ./gradlew publishKmp for native "
    "Maven publishing, retaining KMP .module metadata and all target publications. "
    "Stage locally without credentials using ./gradlew stagePublications "
    "(build/public-maven). Publishing uses https://repo.free-time.me/maven/ and "
    "FURY_TOKEN or furyToken in user ~/.gradle/gradle.properties only. "
    "Native Maven cannot force public visibility: verify_gemfury_release.py must "
    "verify anonymous access before tagging. --dry-run does not validate staging."
)


def main():
    raise SystemExit(MESSAGE)


if __name__ == "__main__":
    main()
