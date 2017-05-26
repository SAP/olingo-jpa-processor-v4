# 1.8 Functions



## Using User Defined Functions

Declaired at an Entity Type

### Scalar Functions used within $filter

## Using Java Classes

Currently only for unbound functions having a function import

Search for all classe taged with interface `ODataFunction`



- Parameterless constructor, new instance for each execution
- method annotated, multiple possible
- return type either primitive or embeddable type
- parameter primitive type -> Has to be annotated as the name is not available via reflection during runtime


```JAVA
class ExampleJavaFunction implements ODataFunction {

  public ExampleJavaOneFunction() {
    super();
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer sum(
      @EdmFunctionParameter(name = "A") int a, @EdmFunctionParameter(name = "A") int b) {
    return a + b;
  }

}
```
