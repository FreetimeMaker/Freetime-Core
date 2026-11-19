"""Fail closed for the retired Gemfury archive upload path."""

MESSAGE = (
    "Remote publishing is disabled. Use ./gradlew stagePublications to stage "
    "KMP .module metadata and platform artifacts locally in build/public-maven."
)


def main():
    raise SystemExit(MESSAGE)


if __name__ == "__main__":
    main()
