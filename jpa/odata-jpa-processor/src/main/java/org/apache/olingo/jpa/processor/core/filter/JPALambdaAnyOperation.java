package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.uri.UriInfoResource;

class JPALambdaAnyOperation extends JPALambdaOperation implements JPAOperator {

  public JPALambdaAnyOperation(JPAFilterComplierAccess jpaComplier, UriInfoResource member) {
    super(jpaComplier, member);
  }

}
