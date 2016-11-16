package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAExpressionOperator extends JPAOperator {
  @Override
  public Expression<Boolean> get() throws ODataApplicationException;

  public Enum<?> getOperator();

}
