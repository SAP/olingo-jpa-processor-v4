package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServiceDocument;
import org.apache.olingo.jpa.processor.core.query.JPAAbstractQuery;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

/**
 * Compiles just one Expression. Mainly build for filter on navigation
 * @author Oliver Grande
 *
 */
//TODO handle $it ...
public class JPAFilterElementComplier extends JPAAbstractFilter {
  final JPAOperationConverter converter;
  final EntityManager em;
  final OData odata;
  final ServiceDocument sd;
  final List<UriResource> uriResourceParts;
  final JPAAbstractQuery parent;
  final VisitableExpression expression;

  public JPAFilterElementComplier(final OData odata, final ServiceDocument sd, final EntityManager em,
      final JPAEntityType jpaEntityType, final JPAOperationConverter converter,
      final List<UriResource> uriResourceParts, final JPAAbstractQuery parent, final VisitableExpression expression) {

    super(jpaEntityType, expression);
    this.converter = converter;
    this.em = em;
    this.odata = odata;
    this.sd = sd;
    this.uriResourceParts = uriResourceParts;
    this.parent = parent;
    this.expression = expression;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAFilterComplier#compile()
   */
  @Override
  @SuppressWarnings("unchecked")
  public Expression<Boolean> compile() throws ExpressionVisitException, ODataApplicationException {
    int handle = parent.getDebugger().startRuntimeMeasurement("JPAFilterCrossComplier", "compile");

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
  public OData getOdata() {
    return odata;
  }

  @Override
  public ServiceDocument getSd() {
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

}
