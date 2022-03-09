# Description

[![Build Status](https://app.travis-ci.com/SAP/olingo-jpa-processor-v4.svg?branch=develop)](https://app.travis-ci.com/github/SAP/olingo-jpa-processor-v4)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=SAP_olingo-jpa-processor-v4&metric=coverage)](https://sonarcloud.io/dashboard?id=SAP_olingo-jpa-processor-v4)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt)
[![REUSE status](https://api.reuse.software/badge/github.com/SAP/olingo-jpa-processor-v4)](https://api.reuse.software/info/github.com/SAP/olingo-jpa-processor-v4)
![GitHub last commit (develop)](https://img.shields.io/github/last-commit/SAP/OLINGO-JPA-PROCESSOR-V4/master.svg)
[![GitHub release](https://img.shields.io/github/release-pre/sap/olingo-jpa-processor-v4.svg?color=orange&label=release)](https://github.com/SAP/olingo-jpa-processor-v4/releases/)

The JPA Processor shall fill the gap between [Olingo V4](https://olingo.apache.org/doc/odata4/index.html) and the database if [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) is used for object-relational mapping.

At the current state the JPA Processor provide support for:

1. Generating OData metadata from JPA metadata.
2. Processing Get requests by converting them into Criteria Builder queries.
3. Supporting entity manipulations.

To get started make use of the [Quicks Start](/jpa-tutorial/QuickStart/QuickStart.md) tutorial.

The tutorials from the previous major version is still available under: [Tutorials](/jpa-tutorial/Tutorials/Introduction/Introduction.md).

## Requirements

The JPA Processor requires, minimum Java version [1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). The current version comes with [Olingo 4.8.0](https://github.com/apache/olingo-odata4). If you want to be updated about Olingo changes subscribe to Olingo's [user mailing list](user-subscribe@olingo.apache.org).

Even so no JPA implementation is preferred, as long as it supports [JSR-338 Java Persistence 2.2](https://jcp.org/en/jsr/detail?id=338), it has to be stated that all test have been performed with [Eclipselink 2.7.9](http://www.eclipse.org/eclipselink/). If you have any problem e.g. with [Hibernate](http://hibernate.org) or [OpenJPA](https://openjpa.apache.org/), create an [issue](https://github.com/SAP/olingo-jpa-processor-v4/issues), but there is no guaranty that it can be solved, as e.g. Hibernate implements some JPA interfaces "differently" than EclipseLink.

## Download and Installation

The JPA Processor is a collection of [Maven](https://maven.apache.org) projects. To use it you need to
clone the repository, import the projects and declare a dependency to either the metadata generation only:

```XML
<dependency>
    <groupId>com.sap.olingo</groupId>
    <artifactId>odata-jpa-metadata</artifactId>
    <version>1.0.8</version>
</dependency>
```

Or to the complete processor:

```XML
<dependency>
    <groupId>com.sap.olingo</groupId>
    <artifactId>odata-jpa-processor</artifactId>
    <version>1.0.8</version>
</dependency>
```

## Limitations

The core of this project became stable. Some of the addons are still in the state of incubation, so some incompatible changes my come up. Nevertheless feel free to use the JPA processor and the addons where ever it helps.

## How to obtain support

For bugs, questions and ideas for enhancement please open an issue in github.

## To-Do (upcoming-changes)

The flowing extensions/changes are planned:

* Tenant depended metadata
* Enable hooks for retrieving data
* Support of $ref
* ETag on $metadata
* Support asynchronous requests
* Parallel processing for $expand
* Overload Operations

New versions will follow [Semantic Versioning](https://semver.org).

## License

Copyright (c) 2016-2021 SAP SE or an SAP affiliate company and olingo-jpa-processor-v4 contributors. Please see our [LICENSE.txt](LICENSE.txt) for copyright and license information.
Detailed information including third-party components and their licensing/copyright information is available via the [REUSE tool](https://api.reuse.software/info/github.com/SAP/olingo-jpa-processor-v4).

## Release Notes

|Version|Changes|Incompatible Changes|
|-- |-- |-- |
|0.2.4|- Switch to Olingo version 4.4.0<br> - Support of Enumeration Types<br>- Support of $count at $expand|Yes|
|0.2.6|- Solution for issue [#21](https://github.com/SAP/olingo-jpa-processor-v4/issues/21)<br> - JPA Join tables can be used e.g. for Many To Many relationship (issue [#22](https://github.com/SAP/olingo-jpa-processor-v4/issues/22)). If such relation shall be used in a filter a corresponding JPA entity is required, which can be hidden from the API using @EdmIgnore. Please be aware that in case both source and target are subtypes Eclipselink (version 2.7.1 used) may get confused when generating a subquery for filtering, see [Bug 529565](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529565) | No|
|0.2.7|- Solution for issue [#29](https://github.com/SAP/olingo-jpa-processor-v4/issues/29)<br> - Solution for issue [#35](https://github.com/SAP/olingo-jpa-processor-v4/issues/35)<br> - Solution for issue [#37](https://github.com/SAP/olingo-jpa-processor-v4/issues/37)<br>|No|
|0.2.8|- Support of Collection Properties<br> - New tutorials 1.7 Suppressing Elements and 1.13 Collection Properties |No|
|0.2.9|- Support on top level server driven paging<br> - New tutorial 4.3 Server Driven Paging |No|  
|0.2.10|- Handling of Content-Id in batch requests<br> - Update to Olingo 4.5.0<br> - Update tutorial 1.6, 3.3, 3.5 and 3.6  |No|  
|0.3.1|- Support of instance based authorizations<br> - Solution for issue [#60](https://github.com/SAP/olingo-jpa-processor-v4/issues/60)<br> - Solution for issue [#49](https://github.com/SAP/olingo-jpa-processor-v4/issues/49)<br> - Correct typo in interface JPAODataPagingProvider <br> - New tutorials 2.3, 4.4  |Yes|
|0.3.2|- Lift unit tests to JUnit 5<br> - Correction of http return codes on empty responses |No|
|0.3.3|- Support of PUT requests on collection properties and simple primitive properties<br> - Update to Olingo 4.6.0 <br> - Solution for issue [#69](https://github.com/SAP/olingo-jpa-processor-v4/issues/69) <br> - Solution for issue [#71](https://github.com/SAP/olingo-jpa-processor-v4/issues/71)<br>- Update tutorial [4.4](jpa-tutorial/Tutorials/SpecialTopics/4-4-InstanceBasedAuthorizations.md)|No
|0.3.4|- Support of $select as part of $expand<br> - Support of field groups<br> - Introduction of a request context, which includes deprecation of methods <br> - Etag now written into response e.g. @odata.etag when JSON was requested<br> - Solution for issue [#78](https://github.com/SAP/olingo-jpa-processor-v4/issues/78) <br> - Updated tutorials: [2.2](jpa-tutorial/Tutorials/RetrieveData/2-2-RetrievingData.md), [2.3](jpa-tutorial/Tutorials/RetrieveData/2-3-UsingFunctions.md), [3.1](jpa-tutorial/Tutorials/ChangeData/3-1-Preparation.md), [3.2](jpa-tutorial/Tutorials/ChangeData/3-2-CreatingEntities.md),  [4.4](jpa-tutorial/Tutorials/SpecialTopics/4-4-InstanceBasedAuthorizations.md)  |Yes|
|0.3.5|- Extension of session context, so an entity manager factory can be provided. This will allow creating Spring based services without `persistence.xml` file <br> - Solution for issue [#85](https://github.com/SAP/olingo-jpa-processor-v4/issues/85)<br> - Usage of Olingo JSON deserializer for CUD requests<br> - Extension of session context, so an own Edm Name Builder can be provided|Yes|
|0.3.6|- Enable more flexible transaction handling<br> - Part solution for issue [#83](https://github.com/SAP/olingo-jpa-processor-v4/issues/83)<br> - Increase support of Spring by performing request mapping in case a mapping path is provided via the service context|No|
|0.3.7| - Update Olingo dependency to 4.7.0|No|
|0.3.8| - Update Olingo dependency to 4.7.1<br> - Support of  `java.time` data types. Prerequisite is the usage of JPA 2.2. <br> - Support of Absolute Context URL. See issue [#103](https://github.com/SAP/olingo-jpa-processor-v4/issues/103)<br> - Temporal data types do not longer require a Precision [#98](https://github.com/SAP/olingo-jpa-processor-v4/issues/98)<br>Support of MappedSuperclass|No|
|0.3.9| - Solutions for issue [#112](https://github.com/SAP/olingo-jpa-processor-v4/issues/112) <br> - Solutions for issue [#114](https://github.com/SAP/olingo-jpa-processor-v4/issues/114)|No|
|0.3.10| - Update Olingo dependency to 4.8.0<br> - Deprecation of ```setExternalName``` in ```IntermediateModelItemAccess```<br>- Solutions for issue [#134](https://github.com/SAP/olingo-jpa-processor-v4/issues/136)<br>- Solution for issue [#136](https://github.com/SAP/olingo-jpa-processor-v4/issues/136) |No|
|0.3.11| - Solutions for issue [#138](https://github.com/SAP/olingo-jpa-processor-v4/issues/138)|No|
|1.0.6|- Transient Properties<br> - Singletons<br> - Entity Types without Entity Set<br> - Rework Request Context<br> - Deprecation of annotation  EdmAsEntitySet   |Yes|
|1.0.8|- Solution for issue [#145](https://github.com/SAP/olingo-jpa-processor-v4/issues/145) |No|