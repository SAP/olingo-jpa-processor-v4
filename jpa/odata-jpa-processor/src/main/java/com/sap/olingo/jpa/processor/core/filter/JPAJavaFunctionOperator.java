package com.sap.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceFunction;

import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.processor.JPAJavaFunctionProcessor;

/**
 * Handle OData Functions that are implemented as user defined data base functions. This will be mapped
 * to JPA criteria builder function().
 *
 * @author Oliver Grande
 *
 */
public final class JPAJavaFunctionOperator implements JPAExpression {
  private final JPAJavaFunction jpaFunction;
  private final UriResourceFunction resource;
  private final JPAODataQueryContext queryContext;
  private final JPAServiceDocument sd;

  public JPAJavaFunctionOperator(final JPAVisitor jpaVisitor, final UriResourceFunction resource,
      final JPAJavaFunction jpaFunction) {

    super();
    this.queryContext = new ODataJPAQueryContext(jpaVisitor);
    this.sd = jpaVisitor.getSd();
    this.resource = resource;
    this.jpaFunction = jpaFunction;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Expression<Boolean> get() throws ODataApplicationException {
    return (Expression<Boolean>) new JPAJavaFunctionProcessor(sd, resource, jpaFunction, queryContext).process();
  }

  @Override
  public String getName() {
    return jpaFunction.getExternalName();
  }
}
