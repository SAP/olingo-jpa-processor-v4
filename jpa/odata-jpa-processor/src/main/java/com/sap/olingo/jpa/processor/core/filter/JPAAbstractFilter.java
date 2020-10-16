package com.sap.olingo.jpa.processor.core.filter;

import java.util.Collections;
import java.util.List;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPAAbstractFilter implements JPAFilterComplier, JPAFilterComplierAccess {
  final JPAEntityType jpaEntityType;
  final VisitableExpression expression;
  final JPAAssociationPath association;

  public JPAAbstractFilter(final JPAEntityType jpaEntityType, final VisitableExpression expression) {
    this(jpaEntityType, expression, null);
  }

  public JPAAbstractFilter(final JPAEntityType jpaEntityType, final UriInfoResource uriResource,
      final JPAAssociationPath association) {
    super();
    this.jpaEntityType = jpaEntityType;
    if (uriResource != null && uriResource.getFilterOption() != null) {
      this.expression = uriResource.getFilterOption().getExpression();
    } else {
      this.expression = null;
    }
    this.association = association;
  }

  public JPAAbstractFilter(final JPAEntityType jpaEntityType, final VisitableExpression expression,
      final JPAAssociationPath association) {
    super();
    this.jpaEntityType = jpaEntityType;
    this.expression = expression;
    this.association = association;
  }

  @Override
  public List<JPAPath> getMember() throws ODataApplicationException {
    final JPAMemberVisitor visitor = new JPAMemberVisitor(jpaEntityType);
    if (expression != null) {
      try {
        expression.accept(visitor);
      } catch (ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
      return Collections.unmodifiableList(visitor.get());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public JPAAssociationPath getAssociation() {
    return association;
  }
}