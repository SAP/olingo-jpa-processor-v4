package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

public interface JPAInOperator<T, X extends JPAOperator> extends JPAExpressionOperator {

  @SuppressWarnings("unchecked")
  @Override
  BinaryOperatorKind getOperator();

  List<X> getFixValues();

  Expression<T> getLeft() throws ODataApplicationException;
}
