package com.sap.olingo.jpa.processor.core.filter;

import jakarta.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;

public interface JPAExpression extends JPAOperator {

  @Override
  Expression<Boolean> get() throws ODataApplicationException;
}