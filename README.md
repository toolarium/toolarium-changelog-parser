[![License](https://img.shields.io/github/license/toolarium/toolarium-changelog-parser)](https://github.com/toolarium/toolarium-changelog-parser/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-changelog-parser/1.1.2)](https://search.maven.org/artifact/com.github.toolarium/toolarium-changelog-parser/1.1.2/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-changelog-parser/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-changelog-parser)


# toolarium-changelog-parser

Implements a parser, validator and formatter for [keepachangelog](https://keepachangelog.com)-formatted `CHANGELOG.md` files. Files in this format can be automatically processed and validated. The validator and formatter also support a rich configuration.

This is part of the [common-gradle-build](https://github.com/toolarium/common-gradle-build/).


## Getting Started

The library follows the [keep a changelog](https://keepachangelog.com/en/1.0.0/) specification and supports:

- Parsing changelog files or strings into a structured DTO model
- Validating changelogs against configurable rules (sort order, change types, release links, YANKED entries, etc.)
- Formatting a changelog object back to the canonical text format
- Running as a standalone CLI tool, including validation of remote changelog files via HTTP/HTTPS


## Concepts

### DTO Model

The object model maps directly to the keepachangelog structure:

```
Changelog
  projectName       - the # heading name
  description       - optional text below the heading
  List<ChangelogEntry>
    releaseVersion  - parsed semantic version (or null for Unreleased)
    releaseDate     - LocalDate
    releaseLink     - optional URL linked to the version bracket
    isReleased      - false for the Unreleased entry
    wasYanked       - true when [YANKED] is present in the heading
    info            - optional extra text in the heading line
    description     - optional text below the version heading
    List<ChangelogSection>
      changeType    - one of the supported ChangelogChangeType values
      List<String>  - comment lines for this section
```

### Change Types

`ChangelogChangeType` defines the supported section headings (`### heading`):

| Value         | Heading text  |
|---------------|---------------|
| `ADDED`       | Added         |
| `CHANGED`     | Changed       |
| `DEPRECATED`  | Deprecated    |
| `REMOVED`     | Removed       |
| `FIXED`       | Fixed         |
| `SECURITY`    | Security      |
| `PERFORMANCE` | Performance   |

### ChangelogConfig

`ChangelogConfig` controls both validation rules and formatting output. All options have defaults that match the keepachangelog specification.

| Option                         | Default | Description |
|--------------------------------|---------|-------------|
| `sectionCharacter`             | `#`     | Heading character |
| `headerSeparator`              | `-`     | Separator between version and date |
| `itemSeparator`                | `-`     | Bullet character for comment items |
| `supportUnreleased`            | `true`  | Allow an `Unreleased` entry |
| `supportEmptySection`          | `false` | Allow sections with no items |
| `supportSpaceAroundVersion`    | `true`  | Allow spaces inside version brackets |
| `supportBracketsAroundVersion` | `true`  | Wrap version in `[ ]` brackets |
| `supportReleaseLink`           | `true`  | Allow a URL linked to the version |
| `supportReleaseInfo`           | `true`  | Allow extra text in the version heading |
| `supportLinkInDescription`     | `true`  | Allow URLs in entry descriptions |
| `supportIdListOnEndOfTheComment` | `true` | Allow `(ID1, ID2)` id lists at the end of comments |
| `linkCommentCheckExpression`   | built-in regex | Regex to detect URLs in comments |
| `idCommentCheckExpression`     | built-in regex | Regex to detect ticket IDs in comments |

### ChangelogParseResult

`parse()` always returns a `ChangelogParseResult` — it never throws on malformed input. The result holds:
- `getChangelog()` — the parsed `Changelog` object (may be incomplete on bad input)
- `getChangelogErrorList()` — a `ChangelogErrorList` with general and per-version errors

### ChangelogErrorList

Errors are grouped by type:
- **General errors** — keyed by `ErrorType`: `CHANGELOG`, `HEADER`, `ENTRIES`, `REFERENCE`, `UNRELEASED`
- **Release errors** — keyed by `ChangelogReleaseVersion`

Use `isEmpty()` to check for any errors, `prepareString()` to get a formatted summary.


## Use it

### Dependency

Include the dependency in your project (current version: **1.1.2**):

#### Gradle

```groovy
dependencies {
    implementation "com.github.toolarium:toolarium-changelog-parser:1.1.2"
}
```

#### Maven

```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-changelog-parser</artifactId>
    <version>1.1.2</version>
</dependency>
```


### API Usage

All operations are accessed through the `ChangelogFactory` singleton:

```java
ChangelogFactory factory = ChangelogFactory.getInstance();
```

#### Parse from a file

```java
ChangelogParseResult result = factory.parse(Paths.get("CHANGELOG.md"));
Changelog changelog = result.getChangelog();

if (!result.getChangelogErrorList().isEmpty()) {
    System.out.println(result.getChangelogErrorList().prepareString());
}
```

#### Parse from a string

```java
ChangelogParseResult result = factory.parse(changelogContent);
```

#### Validate

Validation throws `ValidationException` (which contains the full `ChangelogErrorList`) on any rule violation:

```java
ChangelogConfig config = new ChangelogConfig();
try {
    Changelog changelog = factory.validate(config, Paths.get("CHANGELOG.md"));
} catch (ValidationException e) {
    System.out.println(e.getValidationErrorList().prepareString());
}
```

Optionally assert the expected project name, description and newest version:

```java
Changelog changelog = factory.validate(config, Paths.get("CHANGELOG.md"),
    "my-project",       // expected project name  (or null to skip)
    null,               // expected description   (or null to skip)
    "1.2.3");           // expected newest version (or null to skip)
```

#### Validate a Changelog object directly

```java
factory.validate(config, changelog, null, null, "1.2.3");
```

#### Format

```java
ChangelogConfig config = new ChangelogConfig();
String formatted = factory.format(config, changelog);
```

Or format directly from a file:

```java
String formatted = factory.format(config, Paths.get("CHANGELOG.md"));
```

#### Access entries programmatically

```java
// get the Unreleased entry
ChangelogEntry unreleased = changelog.getEntry("Unreleased");

// get a specific version
ChangelogEntry entry = changelog.getEntry("1.2.3");

// iterate all entries (returned in descending version order)
for (ChangelogEntry entry : changelog.getEntries()) {
    System.out.println(entry.getReleaseVersion() + " - " + entry.getReleaseDate());
    for (ChangelogSection section : entry.getSectionList()) {
        System.out.println("  " + section.getChangeType().getTypeName());
        for (String comment : section.getChangeCommentList()) {
            System.out.println("    - " + comment);
        }
    }
}
```

#### Build a changelog programmatically

```java
Changelog changelog = new Changelog("my-project", "Description of the project.");

ChangelogEntry entry = changelog.addEntry("1.0.0", "2026-01-15");
entry.addSection(ChangelogChangeType.ADDED).add("Initial release.");

String formatted = factory.format(new ChangelogConfig(), changelog);
```


## CLI Tool

Download the self-contained runner JAR:

- [toolarium-changelog-parser-1.1.2-runner.jar](https://repo1.maven.org/maven2/com/github/toolarium/toolarium-changelog-parser/1.1.2/toolarium-changelog-parser-1.1.2-runner.jar)

### Usage

```
java -jar toolarium-changelog-parser-1.1.2-runner.jar [options]

Options:
  --validate <file>   Path to a local CHANGELOG.md file, or an HTTP/HTTPS URL
                      to a remote changelog file (max 10 MB).
  --verbose           Print the formatted changelog after successful validation.
  --no-header         Suppress the version/file header in error output
                      (useful for CI log integration).
```

### Examples

Validate a local file:
```
java -jar toolarium-changelog-parser-1.1.2-runner.jar --validate CHANGELOG.md
```

Validate a remote file:
```
java -jar toolarium-changelog-parser-1.1.2-runner.jar --validate https://raw.githubusercontent.com/toolarium/toolarium-changelog-parser/master/CHANGELOG.md
```

Print the normalised changelog after validation:
```
java -jar toolarium-changelog-parser-1.1.2-runner.jar --validate CHANGELOG.md --verbose
```

Suppress the header (e.g. in CI pipelines):
```
java -jar toolarium-changelog-parser-1.1.2-runner.jar --validate CHANGELOG.md --no-header
```


## Built With

* [cb](https://github.com/toolarium/common-build) - The toolarium common build

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/toolarium/toolarium-changelog-parser/tags).
