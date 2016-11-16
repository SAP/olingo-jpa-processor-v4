package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.BinaryOperatorKind;

public interface JPAArithmeticOperator extends JPAOperator {
  @Override
  Expression<Number> get() throws ODataApplicationException;

  BinaryOperatorKind getOperator();

  Object getRight();

  Expression<Number> getLeft(CriteriaBuilder cb) throws ODataApplicationException;

  Number getRightAsNumber(CriteriaBuilder cb) throws ODataApplicationException;

  Expression<Number> getRightAsExpression() throws ODataApplicationException;

  Expression<Integer> getLeftAsIntExpression() throws ODataApplicationException;

  Expression<Integer> getRightAsIntExpression() throws ODataApplicationException;

}