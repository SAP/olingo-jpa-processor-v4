# OData V4 JPA Processor
## Getting Started
The JPA Processor shall fill the gap between Olingo V4 and the database if JPA is used for object-relational mapping.
At the current state the JPA Processor provide support for:  
1. Generating OData metadata from JPA metadata.  
2. Processing Get requests by converting them into Criteria Builder queries.  
3. Supporting entity manipulations. 
 
More details can be found in the [Tutorial](/jpa-tutorial/Tutorials/Introduction/Introduction.md)    

The project is still in the state of incubation, so some incompatible changes my come up. E.g. for batch request handling, were the _sort order_ problem need to be solved. Nevertheless feel free to use the JPA processor where ever it helps.
## Dependencies
## License
This project is licensed under the Apache Software License Version 2.0, except as noted otherwise in the [License file](/LICENSE.txt).
