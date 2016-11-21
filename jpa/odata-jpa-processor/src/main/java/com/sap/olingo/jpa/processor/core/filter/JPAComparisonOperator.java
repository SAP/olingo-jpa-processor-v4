package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAComparisonOperator<T extends Comparable<T>> extends JPAExpressionOperator {

  Expression<T> getLeft() throws ODataApplicationException;

  Object getRight();

  Comparable<T> getRightAsComparable() throws ODataApplicationException;

  Expression<T> getRightAsExpression() throws ODataApplicationException;

}