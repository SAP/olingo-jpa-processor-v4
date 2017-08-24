# Description
The JPA Processor shall fill the gap between [Olingo V4](https://olingo.apache.org/doc/odata4/index.html) and the database if [JPA](https://en.wikipedia.org/wiki/Java_Persistence_API) is used for object-relational mapping.

At the current state the JPA Processor provide support for:  
1. Generating OData metadata from JPA metadata.  
2. Processing Get requests by converting them into Criteria Builder queries.  
3. Supporting entity manipulations. 
 
More details can be found in the [Tutorials](/jpa-tutorial/Tutorials/Introduction/Introduction.md)    

# Requirements
As of now the JPA Processor requires at least [Java 1.6](http://www.oracle.com/technetwork/java/javase/downloads/jdk6downloads-1902814.html), but it is very likely that in the future the minimum Java version is changed to [1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).

# Download and Installation
Clone the repository.

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
