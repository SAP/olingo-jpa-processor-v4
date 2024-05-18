# Description

[![Build Status](https://app.travis-ci.com/SAP/olingo-jpa-processor-v4.svg?branch=develop)](https://app.travis-ci.com/github/SAP/olingo-jpa-processor-v4)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SAP_olingo-jpa-processor-v4&metric=coverage)](https://sonarcloud.io/dashboard?id=SAP_olingo-jpa-processor-v4)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP/olingo-jpa-processor-v4)](https://api.reuse.software/info/github.com/SAP/olingo-jpa-processor-v4)
![GitHub last commit (develop)](https://img.shields.io/github/last-commit/SAP/OLINGO-JPA-PROCESSOR-V4/main.svg)
[![GitHub release](https://img.shields.io/github/release-pre/sap/olingo-jpa-processor-v4.svg?color=orange&label=release)](https://github.com/SAP/olingo-jpa-processor-v4/releases/)
[![Project Map](https://sourcespy.com/shield.svg)](https://sourcespy.com/github/sapolingojpaprocessorv4/)

The JPA Processor shall fill the gap between [Olingo V4](https://olingo.apache.org/doc/odata4/index.html) and the database, if [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) is used for object-relational mapping. If you want to be updated about Olingo changes subscribe to Olingo's [user mailing list](user-subscribe@olingo.apache.org).

At the current state the JPA Processor provide support for:

1. Generating OData metadata from JPA metadata.
2. Processing Get requests by converting them into Criteria Builder queries.
3. Supporting entity manipulations.

To get started make use of the [Quicks Start](/jpa-tutorial/QuickStart/QuickStart.adoc) tutorial.

The tutorials from the previous major version is still available under: [Tutorials](/jpa-tutorial/Tutorials/Introduction/Introduction.md).

## Requirements

As of now, the JPA Processor has two major versions 1.1.x and 2.x.x.

### 1.1.x

The JPA Processor requires, minimum Java version [1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). The current version comes with [Olingo 4.9.0](https://github.com/apache/olingo-odata4).

Even so no JPA implementation is preferred, as long as it supports [JSR-338 Java Persistence 2.2](https://jcp.org/en/jsr/detail?id=338), it has to be stated that all test have been performed with [Eclipselink 2.7.9](http://www.eclipse.org/eclipselink/). If you have any problem e.g. with [Hibernate](http://hibernate.org) or [OpenJPA](https://openjpa.apache.org/), create an [issue](https://github.com/SAP/olingo-jpa-processor-v4/issues), but there is no guaranty that it can be solved, as e.g. Hibernate implements some JPA interfaces "differently" than EclipseLink.

There is no further development for this major version.

### 2.x.x

The current version is based on [Jakarta 10](https://projects.eclipse.org/releases/jakarta-10), so [JPA 3.1.0](https://projects.eclipse.org/projects/ee4j.jpa/releases/3.1) or [Jakarta Persistence Specification](https://github.com/jakartaee/persistence), receptively and [Jakarta Servlet 6.0](https://projects.eclipse.org/projects/ee4j.servlet/releases/6.0). Test are performed using [Eclipselink 4.0.2](https://projects.eclipse.org/projects/ee4j.eclipselink/releases/4.0.2), but there is no real dependency to a JPA implementation. This version requires Java [17](https://sap.github.io/SapMachine/#download).

The current version comes with [Olingo 5.0.0](https://github.com/apache/olingo-odata4).

## Download and Installation

The JPA Processor is a collection of [Maven](https://maven.apache.org) projects. To use it you need to
clone the repository, import the projects and declare a dependency to either the metadata generation only:

```XML
<dependency>
    <groupId>com.sap.olingo</groupId>
    <artifactId>odata-jpa-metadata</artifactId>
    <version>2.1.1</version>
</dependency>
```

Or to the complete processor:

```XML
<dependency>
    <groupId>com.sap.olingo</groupId>
    <artifactId>odata-jpa-processor</artifactId>
    <version>2.1.1</version>
</dependency>
```

## Limitations

The core of this project became stable. Some of the addons are still in the state of incubation, so some incompatible changes my come up. Nevertheless feel free to use the JPA processor and the addons where ever it helps.

## Contributing

If you want to report a bug or have suggestions to improve the JPA Processor, read up on our [guidelines for contributing](./CONTRIBUTING.md) to learn about our submission process, coding rules and more.

We'd love all and any contributions.

## To-Do (upcoming-changes)

The following extensions/changes are planned:

* Support of method call at $orderby
* Tenant depended metadata
* Enable hooks for retrieving data
* Support of $ref
* ETag on $metadata
* Support asynchronous requests
* Parallel processing for $expand

New versions will follow [Semantic Versioning](https://semver.org).

## License

Copyright (c) 2016-2023 SAP SE or an SAP affiliate company and olingo-jpa-processor-v4 contributors. Please see our [LICENSE.txt](LICENSE.txt) for copyright and license information.
Detailed information including third-party components and their licensing/copyright information is available via the [REUSE tool](https://api.reuse.software/info/github.com/SAP/olingo-jpa-processor-v4).

## Release Notes

|Version| Changes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |Incompatible Changes|
|-- |-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-- |
|1.0.6| - Transient Properties<br> - Singletons<br> - Entity Types without Entity Set<br> - Rework Request Context<br> - Deprecation of annotation  EdmAsEntitySet                                                                                                                                                                                                                                                                                                                                                |Yes|
|1.0.8| - Solution for issue [#145](https://github.com/SAP/olingo-jpa-processor-v4/issues/145)                                                                                                                                                                                                                                                                                                                                                                                                                    |No|
|1.0.9| - Update Olingo dependency to 4.9.0<br> - Solutions for issues [#164](https://github.com/SAP/olingo-jpa-processor-v4/issues/164), [#155](https://github.com/SAP/olingo-jpa-processor-v4/issues/155), [#191](https://github.com/SAP/olingo-jpa-processor-v4/issues/191), [#156](https://github.com/SAP/olingo-jpa-processor-v4/issues/156)<br>                                                                                                                                                             |No|
|1.1.1| - Enable action overload<br> - Basic support of OData annotations<br>- Solution of issues [#207](https://github.com/SAP/olingo-jpa-processor-v4/issues/207), [#211](https://github.com/SAP/olingo-jpa-processor-v4/issues/211), [#212](https://github.com/SAP/olingo-jpa-processor-v4/issues/212), [#213](https://github.com/SAP/olingo-jpa-processor-v4/issues/213), [#214](https://github.com/SAP/olingo-jpa-processor-v4/issues/214),[#218](https://github.com/SAP/olingo-jpa-processor-v4/issues/218) |No|
|2.0.0| - Minimum Java release now 17<br>- Switch to Jakarta Persistence<br> - Support of Spring Boot 3.x<br> - JPAEdmMetadataPostProcessor became an interface                                                                                                                                                                                                                                                                                                                                                   |Yes|
|2.0.2| - Solution for issue [#239](https://github.com/SAP/olingo-jpa-processor-v4/issues/239)<br> - Partial solution for issue [#226](https://github.com/SAP/olingo-jpa-processor-v4/issues/226)<br> - Solution for issue [#238](https://github.com/SAP/olingo-jpa-processor-v4/issues/238) and [#236](https://github.com/SAP/olingo-jpa-processor-v4/issues/236)|No|
|2.1.0| - Enhancement of annotation API<br>- Enhancement of API for server driven paging<br>- Optional support of IN operand <br>- Update to Olingo 5.0.0<br>- Rework $count implementation<br>- Fix problem with $count on collection properties|No|
|2.1.1| - Fix for issue [#292] (https://github.com/SAP/olingo-jpa-processor-v4/issues/292)|No|
