# 1.8 Functions

The JPA Processor provides two options to build OData Functions. First option is to push down the execution of the function to the database by using `User Defined Functions` (UDF). The second option is to use `JAVA Classes`.

## Using User Defined Functions

You may remember that we have introduced in [Tutorial 1.6 (Navigation Properties And Complex Types)](1-6-NavigationAndComplexTypes.md) the entity AdministrativeDivision, which contains information about regions, their hierarchy, the population or the area. We want to use it to implement exemplary overall three functions:

First let's assume that we want to be able to use the population density as filter criteria. Best is to do that right on the database, which means we need a UDF. This UDF has to be made know to the JPA Processor, which is done via `@EdmFunction` that is used to annotate JPA entities.

The Function shall fulfill the following specification:
* It shall take the Area and the Population as input
* It shall return a Double that represents the population density (scalar function)
* It shall not be called directly, so if shall not have an Function Import
* Last but not least it shall not be bound to an instance

The corresponding annotation locks as follows:
```JAVA
    @EdmFunction(
        name = "PopulationDensity",
        functionName = "\"OLINGO\".\"PopulationDensity\"",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(isCollection = false, type = Double.class),
        parameter = {
            @EdmParameter(name = "Area", parameterName = "UnitArea", type = Integer.class),
            @EdmParameter(name = "Population", parameterName = "Population", type = Long.class) })
```
So we have the `name` the function shall have and the `functionName` on the database as well as flags indicating if the function is bound or shall have a function import and the parameter definitions.

The second one is quite similar: The area is given in square meters, but it is usual to give the density as people per square kilometer. For this we want to provide a function that converts the area accordingly. This function shall be unbound as well and shall have no function import, too.

The third function we want to provide shall give us the siblings of a region within the region hierarchy. This one shall have a function import.<br>
(Alternative Siblings could be modeled as a bound function, try it out)

The annotation for all three functions looks as follows:
```JAVA
@EdmFunctions({
    @EdmFunction(
        name = "PopulationDensity",
        functionName = "\"OLINGO\".\"PopulationDensity\"",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(isCollection = false, type = Double.class),
        parameter = {
            @EdmParameter(name = "Area", parameterName = "UnitArea", type = Integer.class),
            @EdmParameter(name = "Population", parameterName = "Population", type = Long.class) }),
    @EdmFunction(
        name = "ConvertToQkm",
        functionName = "\"OLINGO\".\"ConvertToQkm\"",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(isCollection = false, type = Integer.class),
        parameter = {
            @EdmParameter(name = "Area", parameterName = "UnitArea", type = Integer.class) }),
    @EdmFunction(
        name = "Siblings",
        functionName = "\"OLINGO\".\"Siblings\"",
        isBound = false,
        hasFunctionImport = true,
        returnType = @EdmFunction.ReturnType(isCollection = true),
        parameter = {
            @EdmParameter(name = "CodePublisher", parameterName = "\"Publisher\"", type = String.class, maxLength = 10),
            @EdmParameter(name = "CodeID", parameterName = "\"ID\"", type = String.class, maxLength = 10),
            @EdmParameter(name = "DivisionCode", parameterName = "\"Code\"", type = String.class, maxLength = 10) })
})

@IdClass(AdministrativeDivisionKey.class)
@Entity(name = "AdministrativeDivision")
@Table(schema = "\"OLINGO\"", name = "\"AdministrativeDivision\"")
public class AdministrativeDivision {
	...

}
```
You may have noticed that the return type is not explicitly given for Siblings. In such a case it is assumed that the entity shall be used as return type. 

IsComposable and function overload are not supported.

Details about how to apply functions can be found her: [Using Functions](../RetrieveData/2-3-UsingFunctions.md)


## Using Java Classes
If some more complicated calculations or mapping shall be performed it may be appropriate to implement a method of a JAVA class as _function_. To make a class respectively one of its method accessible via an OData request it has to be marked with the tag interface `ODataFunction`. When the metadata are created the JPA Process searches for classes implementing this interface. To reduce the search space in addition a list of top level packages need to be given via:

```JAVA
handler.getJPAODataContext().setTypePackage("tutorial.operations");
```
Please note that currently only unbound functions having a function import are supported.

For the class and the _function_ method following boundary conditions exist:
- The class must provide either a public parameterless constructor or a public constructor that takes an instance of an entity manager as input, as a new instance for each execution is created.
- A method that represents a function must be annotated (see below), multiple methods per class are possible
- The return type must be either primitive type, an embeddable or an entity.  If the function shall return a collection it has to be a subtype of `Collection<T>` and the @ReturnType has to contain the actual type parameter (e.g. String.class), as this information is not accessible via java reflections.
- Parameter have to have a primitive type, all parameter have to be annotated as the name is not available via reflection during runtime.

The first example shall be a very simple function that is able to sum two integer:

```JAVA
package tutorial.operations;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

public class JavaFunctions implements ODataFunction {

	public JavaFunctions() {
		super();
	}

	@EdmFunction(name = "", returnType = @ReturnType)
	public Integer sum(@EdmParameter(name = "Summand1") int a, @EdmParameter(name = "Summand2") int b) {
		return a + b;
	}
}
```

The method is annotated with the already known `@EdmFunction` annotation. In case the required fields are left empty, like here, the necessary information is taken from the method definition. In fact the return type has be given only if additional facet details shall be provided.

Next step: [Tutorial 1.9 Changing Generated Metadata](1-9-ChangingGeneratedMetadata.md)