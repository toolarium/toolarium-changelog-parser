# toolarium-changelog-parser

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 1.1.0 ] - 2026-05-14
### Added
- New change type PERFORMANCE in ChangelogChangeType.

### Changed
- Replaced jptools dependency with toolarium-common and toolarium-ansi.
- Refactored ChangelogMain to remove jptools dependency, using built-in argument parsing.
- Improved DTO encapsulation: getEntries, getSectionList, getChangeCommentList, getGeneralErrors and getReleaseErrors now return unmodifiable collections.
- Improved URL regex pattern in ChangelogConfig.

### Fixed
- Fixed invalid reference version error message displaying null instead of the input version.
- Fixed setReleaseDate to default to current date only when input is null.

## [ 1.0.0 ] - 2022-02-17
### Security
- Security updates.

## [ 0.9.2 ] - 2022-01-09
### Changed
- Updated to newer dependency versions.

### Fixed
- Heading spaces in items.

## [ 0.9.1 ] - 2021-06-04
### Added
- Support space around brackets.

## [ 0.9.0 ] - 2021-06-04
### Added
- DTO methods to get / add  / remove entries.

### Changed
- DTO data type for setReleaseDate.
- Tests for Changelog class.
- Validation tests.

## [ 0.8.3 ] - 2021-05-24
### Changed
- Updated error messsage.
- Ignore duplicated messages (parsing / validation).
