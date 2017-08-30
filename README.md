# Description
The JPA Processor shall fill the gap between [Olingo V4](https://olingo.apache.org/doc/odata4/index.html) and the database if [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) is used for object-relational mapping.

At the current state the JPA Processor provide support for:  
1. Generating OData metadata from JPA metadata.  
2. Processing Get requests by converting them into Criteria Builder queries.  
3. Supporting entity manipulations.

More details can be found in the [Tutorials](/jpa-tutorial/Tutorials/Introduction/Introduction.md).

# Requirements
The JPA Processor requires, others than Olingo, minimum Java version  [1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html). The current version comes with Olingo 4.3.0. If later versions of OLingo are available this may change.

Even so no JPA implementation is preferred, as long as it supports [JSR-338 Java Persistence 2.1](https://jcp.org/en/jsr/detail?id=317), it has to be stated that all test have been performed with [Eclipselink 2.6.2](http://www.eclipse.org/eclipselink/). If you have any isusse with e.g. [Hibernate](http://hibernate.org) create an issue in github.

# Download and Installation
The JPA Processor is a collection of [Maven](https://maven.apache.org) projects. To use it you need to 
clone the repository, import the projects and declare a dependency to either the metadata generation only:

```
<dependency>
	<groupId>com.sap.olingo</groupId>
	<artifactId>odata-jpa-metadata</artifactId>
	<version>0.1.9-SNAPSHOT</version>
</dependency>
```

Or to the complete processor:

```
<dependency>
	<groupId>com.sap.olingo</groupId>
	<artifactId>odata-jpa-processor</artifactId>
	<version>0.1.9-SNAPSHOT</version>
</dependency>
```

# Limitations
The project is still in the state of incubation, so some incompatible changes my come up, see To-Do. Nevertheless feel free to use the JPA processor where ever it helps.

# How to obtain support
For bugs, questions and ideas for enhancement please open an issue in github.

# To-Do (upcoming-changes)
The flowing extensions/changes are planned:

* Rework batch request handling, to handle the _sort order_ problem.
* Enable hooks for retrieving data.  
* Parallel processing for $expand.
* Enable collection attributes.   


# License
Copyright (c) 2017 SAP SE or an SAP affiliate company. All rights reserved.    
This file is licensed under the Apache Software License, v.2 except as noted otherwise in the [License file](/LICENSE.txt).
