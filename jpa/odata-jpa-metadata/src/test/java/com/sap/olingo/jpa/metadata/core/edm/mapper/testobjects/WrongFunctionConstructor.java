package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

public class WrongFunctionConstructor implements ODataFunction {

  public WrongFunctionConstructor(final JPAODataQueryContext stmt, final EntityManager em) {
    super();
  }

  @EdmFunction(name = "", returnType = @ReturnType)
  public Integer sum(
      @EdmParameter(name = "A") final short a, @EdmParameter(name = "B") final int b) {
    return a + b;
  }
}
