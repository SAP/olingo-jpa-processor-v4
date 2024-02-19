package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public abstract class JPANavigationCountQuery extends JPANavigationSubQuery {

  JPANavigationCountQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType type,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider,
      final List<UriParameter> keyPredicates) throws ODataApplicationException {
    super(odata, sd, type, em, parent, from, association, claimsProvider, keyPredicates);

    this.aggregationType = UriResourceKind.count;
  }

  /**
   * Creates a exist sub query including the where clause joining this query with the parent query
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQuery(final Subquery<?> childQuery,
      final VisitableExpression expression, final List<Path<Comparable<?>>> inPath) throws ODataApplicationException {

    if (childQuery != null)
      // A count query should be the last in a chain. Therefore childQuery has to be null
      throw new ODataJPAQueryException(QUERY_PREPARATION_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR);
    final Subquery<T> query = (Subquery<T>) this.subQuery;
    if (this.association.getJoinTable() != null) {
      if (isCollectionProperty)
        createSubQueryCollectionProperty();
      else
        createSubQueryJoinTableAggregation();
    } else {
      createSubQueryAggregation(query);
    }
    return query;
  }

  protected abstract <T> void createSubQueryAggregation(final Subquery<T> query) throws ODataApplicationException;

  protected abstract void createSubQueryJoinTableAggregation() throws ODataApplicationException;

  protected abstract void createSubQueryCollectionProperty() throws ODataApplicationException;

  protected void createHaving(final Subquery<?> subQuery) throws ODataApplicationException {
    try {
      subQuery.having(this.filterComplier.compile());
    } catch (final ExpressionVisitException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
