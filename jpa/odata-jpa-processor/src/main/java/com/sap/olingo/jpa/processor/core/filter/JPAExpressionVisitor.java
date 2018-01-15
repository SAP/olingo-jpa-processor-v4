package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.From;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;

public interface JPAExpressionVisitor extends ExpressionVisitor<JPAOperator> {

  public OData getOdata();

  public From<?, ?> getRoot();

}
