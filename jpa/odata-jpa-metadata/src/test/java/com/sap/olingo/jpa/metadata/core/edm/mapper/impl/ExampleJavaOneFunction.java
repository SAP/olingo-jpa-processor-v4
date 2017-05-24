package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

class ExampleJavaOneFunction implements ODataFunction {

  private ExampleJavaOneFunction() {
    super();
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer sum(int a, int b) {
    return a + b;
  }

}
