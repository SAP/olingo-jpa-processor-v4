package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

final class JPAMethodBasedExpression extends JPAMethodCallImp implements JPAExpression {

  public JPAMethodBasedExpression(JPAOperationConverter converter, MethodKind methodCall,
      List<JPAOperator> parameters) {
    super(converter, methodCall, parameters);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return (Expression<Boolean>) super.get();
  }
}
