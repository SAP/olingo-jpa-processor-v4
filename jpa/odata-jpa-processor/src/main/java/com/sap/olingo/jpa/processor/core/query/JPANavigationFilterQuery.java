package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPANavigationFilterQuery extends JPANavigationSubQuery implements ExistsExpressionValue {

  JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final From<?, ?> from, final Optional<JPAODataClaimProvider> claimsProvider,
      final List<UriParameter> keyPredicates) throws ODataApplicationException {

    super(odata, sd, jpaEntityType, em, parent, from, association,
        claimsProvider, keyPredicates);

    this.filterComplier = null;
    this.aggregationType = null;
  }

  JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntityType,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider,
      final List<UriParameter> keyPredicates) throws ODataApplicationException {
    super(odata, sd, jpaEntityType, em, parent, from, association, claimsProvider, keyPredicates);
  }

  /**
   * Creates a exist sub query including the where clause joining this query with the parent query
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQuery(final Subquery<?> childQuery,
      final VisitableExpression expression, final List<Path<Comparable<?>>> inPath) throws ODataApplicationException {

    if (this.association.getJoinTable() != null) {
      createSubQueryJoinTable();
    } else {
      createSubQuery(childQuery, expression, inPath);
    }
    return (Subquery<T>) this.subQuery;
  }

  @SuppressWarnings("unchecked")
  protected <T> void createSubQuery(final Subquery<T> childQuery,
      @Nullable final VisitableExpression expression, final List<Path<Comparable<?>>> inPath)
      throws ODataApplicationException {

    createSelectClauseJoin(subQuery, queryRoot, determineAggregationRightColumns(), false);
    Expression<Boolean> whereCondition = null;
    whereCondition = addWhereClause(
        createWhereByAssociation(from, queryRoot, determineJoinColumns()),
        createWhereByKey(queryRoot, this.keyPredicates, jpaEntity));
    if (childQuery != null) {
      whereCondition = cb.and(whereCondition,
          ExpressionUtility.createSubQueryBasedExpression((Subquery<List<Comparable<?>>>) childQuery, inPath, cb,
              expression));
    }
    whereCondition = addWhereClause(whereCondition,
        createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));
    subQuery.where(applyAdditionalFilter(whereCondition));
  }

  /**
   * <pre>
   * SELECT t0."TeamKey"
   *     FROM "OLINGO"."Team" t0
   *     WHERE (EXISTS (
   *         SELECT t2."TeamID"
   *             FROM "OLINGO"."BusinessPartner" t1, "OLINGO"."Membership" t2
   *             WHERE t2."TeamID" = t0."TeamKey"
   *             AND t1."ID" = t2."PersonID"
   *             AND t1."Type" = '1'
   *             AND t1."NameLine2" = 'Mustermann'))
   * </pre>
   */
  protected void createSubQueryJoinTable() throws ODataApplicationException {
    try {
      final List<JPAOnConditionItem> left = association
          .getJoinTable()
          .getJoinColumns(); // Team -->
      final List<JPAOnConditionItem> right = association
          .getJoinTable()
          .getInverseJoinColumns(); // Person -->
      createSelectClauseJoin(subQuery, queryRoot, determineAggregationRightColumns(), false);
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
