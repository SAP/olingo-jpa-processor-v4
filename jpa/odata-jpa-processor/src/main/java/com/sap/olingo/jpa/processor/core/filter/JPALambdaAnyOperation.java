package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.uri.queryoption.expression.Member;

final class JPALambdaAnyOperation extends JPALambdaOperation implements JPAExpressionOperator {

  JPALambdaAnyOperation(final JPAFilterComplierAccess jpaComplier, final Member member) {
    super(jpaComplier, member);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Enum<?> getOperator() {
    return null;
  }

}
