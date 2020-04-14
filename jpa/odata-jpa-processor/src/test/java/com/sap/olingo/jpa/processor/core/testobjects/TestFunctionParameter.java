package com.sap.olingo.jpa.processor.core.testobjects;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

public class TestFunctionParameter implements ODataFunction {
  public static int calls;
  public static int param1;
  public static int param2;
  public EntityManager em;

  public TestFunctionParameter(EntityManager em) {
    super();
    this.em = em;
  }

  @EdmFunction(returnType = @ReturnType)
  public Integer sum(@EdmParameter(name = "A") Integer a, @EdmParameter(name = "B") Integer b) {
    calls += 1;
    param1 = a;
    param2 = b;
    return a + b;
  }
}
