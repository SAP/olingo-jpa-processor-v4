package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

public class ExampleJavaTwoParameterConstructor implements ODataFunction {

  public ExampleJavaTwoParameterConstructor(EntityManager em, Integer a) {
    super();
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer sum(
      @EdmFunctionParameter(name = "A") short a, @EdmFunctionParameter(name = "B") int b) {
    return a + b;
  }

}
