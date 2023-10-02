package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPACountExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAInvertibleVisitableExpression;

public final class JPANavigationFilterQuery extends JPANavigationSubQuery {

  public JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final From<?, ?> from, final Optional<JPAODataClaimProvider> claimsProvider) throws ODataApplicationException {

    super(odata, sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em, parent, from, association,
        claimsProvider, Utility.determineKeyPredicates(uriResourceItem));

    this.filterComplier = null;
    this.aggregationType = null;
  }

  JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType type,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider,
      final List<UriParameter> keyPredicates) throws ODataApplicationException {
    super(odata, sd, type, em, parent, from, association, claimsProvider, keyPredicates);
  }

  /**
   * Creates a exist sub query including the where clause joining this query with the parent query
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQuery(final Subquery<?> childQuery,
      final VisitableExpression expression) throws ODataApplicationException {

    final Subquery<T> query = (Subquery<T>) this.subQuery;
    if (this.association.getJoinTable() != null) {
      createSubQueryJoinTable();
    } else {
      createSubQuery(childQuery, query, expression);
    }
    return query;
  }

  private <T> void createSubQuery(final Subquery<?> childQuery, final Subquery<T> query,
      @Nullable final VisitableExpression expression) throws ODataApplicationException {

    createSelectClauseJoin(query, queryRoot, determineAggregationRightColumns());
    Expression<Boolean> whereCondition = null;
    whereCondition = addWhereClause(
        createWhereByAssociation(from, queryRoot, determineJoinColumns()),
        createWhereByKey(queryRoot, this.keyPredicates, jpaEntity));
    if (childQuery != null) {
      whereCondition = cb.and(whereCondition, createExists(childQuery, expression));
    }
    whereCondition = addWhereClause(whereCondition,
        createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));

    query.where(applyAdditionalFilter(whereCondition));
  }

  private Predicate createExists(final Subquery<?> childQuery, @Nullable final VisitableExpression expression) {

    if (expression instanceof JPACountExpression
        && ((JPAInvertibleVisitableExpression) expression).isInversionRequired()) {
      ((JPAInvertibleVisitableExpression) expression).inversionPerformed();
      return cb.not(cb.exists(childQuery));
    }
    return cb.exists(childQuery);
  }

  private void createSubQueryJoinTable() throws ODataApplicationException {
    /*
     * SELECT t0."TeamKey"
     * FROM "OLINGO"."Team" t0
     * WHERE (EXISTS (SELECT t2."TeamID"
     * FROM "OLINGO"."BusinessPartner" t1, "OLINGO"."Membership" t2
     * WHERE t2."TeamID" = t0."TeamKey"
     * AND t1."ID" = t2."PersonID"
     * AND t1."Type" = '1'
     * AND t1."NameLine2" = 'Mustermann'))
     */
    try {
      final List<JPAOnConditionItem> left = association
          .getJoinTable()
          .getJoinColumns(); // Team -->
      final List<JPAOnConditionItem> right = association
          .getJoinTable()
          .getInverseJoinColumns(); // Person -->
      createSelectClauseJoin(subQuery, queryRoot, determineAggregationRightColumns());
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, left);
      whereCondition = cb.and(whereCondition, createWhereByAssociation(queryJoinTable, queryRoot, right));
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          queryRoot));
      subQuery.where(applyAdditionalFilter(whereCondition));
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

}
