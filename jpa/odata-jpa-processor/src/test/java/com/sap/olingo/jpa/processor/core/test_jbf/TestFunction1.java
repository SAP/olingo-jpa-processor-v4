package com.sap.olingo.jpa.processor.core.test_jbf;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

public class TestFunction1 implements ODataFunction {
  public static int calls;
  public static EntityManager em;
  public static int param1;
  public static int param2;

  public TestFunction1(EntityManager em) {
    super();
    TestFunction1.em = em;
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer sum(@EdmFunctionParameter(name = "A") Integer a, @EdmFunctionParameter(name = "B") Integer b) {
    calls += 1;
    param1 = a;
    param2 = b;
    return a + b;
  }
}
