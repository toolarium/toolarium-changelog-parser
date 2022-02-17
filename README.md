[![License](https://img.shields.io/github/license/toolarium/toolarium-changelog-parser)](https://github.com/toolarium/toolarium-changelog-parser/blob/master/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-changelog-parser/1.0.0)](https://search.maven.org/artifact/com.github.toolarium/toolarium-changelog-parser/1.0.0/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-changelog-parser/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-changelog-parser)


# toolarium-changelog-parser

Implements a parser, validator and formatter for [keepachangelog](https://keepachangelog.com)-formatted `CHANGELOG.md` files. Files in this format can be automated processed and validated. The validator and formatter have additional also a configuration.

This is part of the [common-gradle-build](https://github.com/toolarium/common-gradle-build/).


## Getting Started

The project implements a compatible [keep a changelog](https://keepachangelog.com/en/1.0.0/) parser / validator and formater.


## Use it

To us this in your project, include the dependency (by now the newest version is **1.0.0**):

### CMD:

- Download newest [toolarium-changelog-parser-x.y.z-runner.jar](https://repo1.maven.org/maven2/com/github/toolarium/toolarium-changelog-parser/1.0.0/toolarium-changelog-parser-1.0.0-runner.jar)
- Call: ```java -jar toolarium-changelog-parser-x.y.z-runner.jar --validate CHANGELOG.md```


### Gradle:

```groovy
dependencies {
    implementation "com.github.toolarium:toolarium-changelog-parser:x.y.z"
}
```

### Maven:

```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-changelog-parser</artifactId>
    <version>x.y.z</version>
</dependency>
```


## Built With

* [cb](https://github.com/toolarium/common-build) - The toolarium common build

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/toolarium/toolarium-changelog-parser/tags).