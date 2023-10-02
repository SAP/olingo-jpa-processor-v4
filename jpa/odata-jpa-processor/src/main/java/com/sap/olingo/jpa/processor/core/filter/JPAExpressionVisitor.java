package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.From;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;

public interface JPAExpressionVisitor extends ExpressionVisitor<JPAOperator> {

  public OData getOData();

  public From<?, ?> getRoot();

  public JPAEntityType getEntityType();

}
