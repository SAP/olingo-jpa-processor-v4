package com.sap.olingo.jpa.processor.core.testobjects;

import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

public class TestFunctionParameter implements ODataFunction {
  public static int calls;
  public static int param1;
  public static int param2;
  public EntityManager em;

  public TestFunctionParameter(final EntityManager em) {
    super();
    this.em = em;
  }

  @EdmFunction(returnType = @ReturnType)
  public Integer sum(@EdmParameter(name = "A") final Integer a, @EdmParameter(name = "B") final Integer b) {
    calls += 1;
    param1 = a;
    param2 = b;
    return a + b;
  }

  @EdmFunction(returnType = @ReturnType)
  public Integer sumThrowsException(@EdmParameter(name = "A") final Integer a) throws ODataApplicationException {
    throw new ODataApplicationException("Test", HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), Locale.ENGLISH);
  }

  // Bound Java Functions not supported yet
//  @EdmFunction(returnType = @ReturnType, isBound = true)
//  public Integer bound(@EdmParameter(name = "A") final Person p) {
//    return Integer.valueOf(p.getID());
//  }
}
