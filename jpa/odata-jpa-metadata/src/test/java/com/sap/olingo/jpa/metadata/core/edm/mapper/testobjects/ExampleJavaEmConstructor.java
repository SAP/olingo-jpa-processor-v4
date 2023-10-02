package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

public class ExampleJavaEmConstructor implements ODataFunction, ODataAction {

  public ExampleJavaEmConstructor(final EntityManager em) {
    super();
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer sum(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a + b;
  }

  @EdmAction(name = "")
  public void mul(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {}
}
