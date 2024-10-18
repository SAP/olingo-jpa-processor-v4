package com.sap.olingo.jpa.processor.core.testobjects;

import java.time.LocalDate;

import jakarta.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

public class TestFunctionActionConstructor implements ODataFunction, ODataAction {

  private final EntityManager em;
  private final JPAHttpHeaderMap header;
  private final JPARequestParameterMap parameter;

  public TestFunctionActionConstructor(final EntityManager em, final JPAHttpHeaderMap header,
      final JPARequestParameterMap parameter) {
    super();
    this.em = em;
    this.header = header;
    this.parameter = parameter;
  }

  @EdmFunction(returnType = @ReturnType(type = Boolean.class), hasFunctionImport = true, isBound = false)
  public Boolean func(@EdmParameter(name = "date") final LocalDate date) {
    return em != null && header != null && parameter != null;
  }

  @EdmAction(returnType = @ReturnType)
  public void action(@EdmParameter(name = "date") final LocalDate date) throws Exception {
    if (em == null || header == null || parameter == null)
      throw new Exception("Missing parameter");
  }

  @EdmFunction(returnType = @ReturnType)
  public Boolean funcEnum(@EdmParameter(name = "access") final FileAccess value) {
    return em != null && header != null && parameter != null;
  }
}
