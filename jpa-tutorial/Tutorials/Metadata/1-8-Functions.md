# 1.8 Functions

The JPA Processor provides to options to realize OData Functions. First to use User Defined Functuns, which are defined on the database. Second JAVA Classes.

## Using User Defined Functions

Declared at an Entity Type

### Scalar Functions used within $filter

`.../AdministrativeDivisions?$filter=Tutorial.PopulationDensity(Area=$it/Area,Population=$it/Population) mul 1000000 gt 1000`

`.../AdministrativeDivisions?$filter=Tutorial.PopulationDensity(Area=Tutorial.ConvertToSKm(Area=$it/Area),Population=$it/Population) gt 1000`

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
- Parameter have to have a primitive type, all parameter have to be annotated as the name is not available via reflection during runtime

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

