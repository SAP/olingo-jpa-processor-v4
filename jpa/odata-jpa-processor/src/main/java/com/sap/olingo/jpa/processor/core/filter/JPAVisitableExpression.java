package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

public interface JPAVisitableExpression extends VisitableExpression {

  public UriInfoResource getMember();

  public Literal getLiteral();
}
