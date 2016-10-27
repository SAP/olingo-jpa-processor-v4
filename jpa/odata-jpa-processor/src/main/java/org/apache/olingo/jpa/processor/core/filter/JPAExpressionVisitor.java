package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Root;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;

public interface JPAExpressionVisitor extends ExpressionVisitor<JPAOperator> {

  public OData getOdata();

  public Root<?> getRoot();

}
