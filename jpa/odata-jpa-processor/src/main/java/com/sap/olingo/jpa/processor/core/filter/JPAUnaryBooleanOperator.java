package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

public interface JPAUnaryBooleanOperator extends JPAExpressionOperator {

  @Override
  public Expression<Boolean> get() throws ODataApplicationException;

  public Expression<Boolean> getLeft() throws ODataApplicationException;

  @SuppressWarnings("unchecked")
  @Override
  public UnaryOperatorKind getOperator();

}