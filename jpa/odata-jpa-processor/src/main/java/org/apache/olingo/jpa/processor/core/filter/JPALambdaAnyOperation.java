package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.uri.queryoption.expression.Member;

class JPALambdaAnyOperation extends JPALambdaOperation implements JPAOperator {

  public JPALambdaAnyOperation(JPAFilterComplierAccess jpaComplier, Member member) {
    super(jpaComplier, member);
  }

}
