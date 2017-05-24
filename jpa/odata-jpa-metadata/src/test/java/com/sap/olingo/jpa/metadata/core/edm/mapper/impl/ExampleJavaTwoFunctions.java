package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

class ExampleJavaTwoFunctions implements ODataFunction {

  private ExampleJavaTwoFunctions() {
    super();
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer multi(int a, int b) {
    return a * b;
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer divide(int a, int b) {
    return a / b;
  }
}
