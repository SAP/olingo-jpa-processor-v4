package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPABooleanOperator extends JPAExpressionOperator {

  Expression<Boolean> getLeft() throws ODataApplicationException;

  Expression<Boolean> getRight() throws ODataApplicationException;

}