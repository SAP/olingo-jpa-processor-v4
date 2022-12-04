package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.query.JPAAbstractQuery;

/**
 * Compiles just one Expression. Mainly build for filter on navigation
 * @author Oliver Grande
 *
 */
//TODO handle $it ...
public final class JPAFilterElementComplier extends JPAAbstractFilter {
  final JPAOperationConverter converter;
  final EntityManager em;
  final OData odata;
  final JPAServiceDocument sd;
  final List<UriResource> uriResourceParts;
  final JPAAbstractQuery parent;
  final List<String> groups;

  public JPAFilterElementComplier(final OData odata, final JPAServiceDocument sd, final EntityManager em,
      final JPAEntityType jpaEntityType, final JPAOperationConverter converter,
      final List<UriResource> uriResourceParts, final JPAAbstractQuery parent, final VisitableExpression expression,
      final JPAAssociationPath association, final List<String> groups) {

    super(jpaEntityType, expression, association);
    this.converter = converter;
    this.em = em;
    this.odata = odata;
    this.sd = sd;
    this.uriResourceParts = uriResourceParts;
    this.parent = parent;
    this.groups = groups;

  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier#compile()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException {
    final int handle = parent.getDebugger().startRuntimeMeasurement("JPAFilterCrossComplier", "compile");

    final ExpressionVisitor<JPAOperator> visitor = new JPAVisitor(this);
    final Expression<Boolean> finalExpression = (Expression<Boolean>) expression.accept(visitor).get();

    parent.getDebugger().stopRuntimeMeasurement(handle);
    return finalExpression;
  }

  @Override
  public JPAOperationConverter getConverter() {
    return converter;
  }

  @Override
  public JPAEntityType getJpaEntityType() {
    return jpaEntityType;
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public OData getOData() {
    return odata;
  }

  @Override
  public JPAServiceDocument getSd() {
    return sd;
  }

  @Override
  public List<UriResource> getUriResourceParts() {
    return uriResourceParts;
  }

  @Override
  public JPAAbstractQuery getParent() {
    return parent;
  }

  public VisitableExpression getExpressionMember() {
    return expression;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <S, T> From<S, T> getRoot() {
    return (From<S, T>) parent.getRoot();
  }

  @Override
  public JPAServiceDebugger getDebugger() {
    return parent.getDebugger();
  }

  @Override
  public Optional<JPAODataClaimProvider> getClaimsProvider() {
    return Optional.empty();
  }

  @Override
  public List<String> getGroups() {
    return groups;
  }
}
