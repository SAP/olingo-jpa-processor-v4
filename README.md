# Description
The JPA Processor shall fill the gap between [Olingo V4](https://olingo.apache.org/doc/odata4/index.html) and the database if [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) is used for object-relational mapping.

At the current state the JPA Processor provide support for:
1. Generating OData metadata from JPA metadata.
2. Processing Get requests by converting them into Criteria Builder queries.
3. Supporting entity manipulations.

More details can be found in the [Tutorials](/jpa-tutorial/Tutorials/Introduction/Introduction.md).

# Requirements
The JPA Processor requires, others than Olingo, minimum Java version  [1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). The current version comes with Olingo 4.5.0. If later versions of OLingo are available this may change.

Even so no JPA implementation is preferred, as long as it supports [JSR-338 Java Persistence 2.1](https://jcp.org/en/jsr/detail?id=317), it has to be stated that all test have been performed with [Eclipselink 2.6.2](http://www.eclipse.org/eclipselink/). If you have any isusse with e.g. [Hibernate](http://hibernate.org) create an issue in github.

# Download and Installation
The JPA Processor is a collection of [Maven](https://maven.apache.org) projects. To use it you need to
clone the repository, import the projects and declare a dependency to either the metadata generation only:

```
<dependency>
	<groupId>com.sap.olingo</groupId>
	<artifactId>odata-jpa-metadata</artifactId>
	<version>0.2.10-SNAPSHOT</version>
</dependency>
```

Or to the complete processor:

```
<dependency>
	<groupId>com.sap.olingo</groupId>
	<artifactId>odata-jpa-processor</artifactId>
	<version>0.2.10
```

# Limitations
The project is still in the state of incubation, so some incompatible changes my come up, see To-Do. Nevertheless feel free to use the JPA processor where ever it helps.

# How to obtain support
For bugs, questions and ideas for enhancement please open an issue in github.

# To-Do (upcoming-changes)
The flowing extensions/changes are planned:

* Content-ID handling in $batch requests
* Enable hooks for retrieving data.
* Support asynchronous requests
* Parallel processing for $expand.
* Enhancements on collection attributes.
* Enable server side paging an $expand
* Support of $ref


# License
Copyright (c) 2017 SAP SE or an SAP affiliate company. All rights reserved.
This file is licensed under the Apache Software License, v.2 except as noted otherwise in the [License file](/LICENSE.txt).

# Release Notes

[![Build Status](https://travis-ci.org/SAP/olingo-jpa-processor-v4.svg?branch=develop)](https://travis-ci.org/SAP/odata-v4-jpa-processor)
[![Coverage Status](https://coveralls.io/repos/github/SAP/olingo-jpa-processor-v4/badge.svg?branch=develop)](https://coveralls.io/github/SAP/olingo-jpa-processor-v4?branch=develop)

|Version|Changes|Incompatible Changes|
|-- |-- |-- |
|0.2.4|- Switch to Olingo version 4.4.0<br> - Support of Enumeration Types<br>- Support of $count at $expand|Yes|
|0.2.6|- Solution for issue [#21](https://github.com/SAP/olingo-jpa-processor-v4/issues/21)<br> - JPA Join tables can be used e.g. for Many To Many relationship (issue [#22](https://github.com/SAP/olingo-jpa-processor-v4/issues/22)). If such relation shall be used in a filter a corresponding JPA entity is required, which can be hidden from the API using @EdmIgnore. Please be aware that in case both source and target are subtypes Eclipselink (version 2.7.1 used) may get confused when generating a subquery for filtering, see [Bug 529565](https://bugs.eclipse.org/bugs/show_bug.cgi?id=529565) | No|
|0.2.7|- Solution for issue [#29](https://github.com/SAP/olingo-jpa-processor-v4/issues/29)<br> - Solution for issue [#35](https://github.com/SAP/olingo-jpa-processor-v4/issues/35)<br> - Solution for issue [#37](https://github.com/SAP/olingo-jpa-processor-v4/issues/37)<br>|No|
|0.2.8|- Support of Collection Properties<br> - New tutorials 1.7 Suppressing Elements and 1.13 Collection Properties |No|
|0.2.9|- Support on top level server driven paging<br> - New tutorial 4.3 Server Driven Paging |No|  
|0.2.10|- Handling of Content-Id in batch requests<br> - Update to Olingo 4.5.0<br> - Update tutorial 1.6, 3.3, 3.5 and 3.6  |No|  
