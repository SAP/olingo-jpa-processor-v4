package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

public class ExampleJavaOneFunction implements ODataFunction {

  public ExampleJavaOneFunction() {
    super();
  }

  @EdmFunction(returnType = @ReturnType, hasFunctionImport = true)
  public Integer sum(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a + b;
  }

}
