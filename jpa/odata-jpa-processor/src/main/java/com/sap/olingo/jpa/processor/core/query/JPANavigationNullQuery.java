package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Generates sub queries to be used with an EXISTS condition to fulfill null checks on <i>to one</i> association like
 * <i>AdministrativeDivisions?$filter=Parent eq null</i>
 * <p>
 * EXISTS is preferred over IN, as it showed equal or better performance on PostgreSQL and SAP HANA
 * @author Oliver Grande
 * @since 1.1.1
 * 20.11.2023
 */
public final class JPANavigationNullQuery extends JPANavigationSubQuery implements ExistsExpressionValue {

  JPANavigationNullQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType type,
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
      final VisitableExpression expression, final List<Path<Comparable<?>>> inPath) throws ODataApplicationException {

    if (childQuery != null)
      // A count query should be the last in a chain. Therefore childQuery has to be null
      throw new ODataJPAQueryException(QUERY_PREPARATION_ERROR, HttpStatusCode.INTERNAL_SERVER_ERROR);
    if (this.association.getJoinTable() != null) {
      createSubQueryJoinTableNull();
    } else {
      createSubQueryNull((Subquery<T>) this.subQuery);
    }
    return (Subquery<T>) subQuery;
  }

  /**
   * Example SQL query generated:
   *
   * <pre>
   * SELECT *
   *    FROM "OLINGO"."AdministrativeDivision" E0
   *    WHERE EXISTS (
   *       SELECT E2."CodePublisher" S0
   *         FROM "OLINGO"."AdministrativeDivision" E2
   *         WHERE (((E0."ParentDivisionCode" = E2."DivisionCode")
   *         AND   (E0."ParentCodeID" = E2."CodeID"))
   *         AND   (E0."CodePublisher" = E2."CodePublisher"))
   *         GROUP BY E2."CodePublisher", E2."CodeID", E2."DivisionCode")
   * </pre>
   *
   * @param <T>
   * @param childQuery
   * @param query
   * @throws ODataApplicationException
   */
  private <T> void createSubQueryNull(final Subquery<T> query)
      throws ODataApplicationException {

    createSelectClauseJoin(query, queryRoot, determineAggregationRightColumns(), false);
    Expression<Boolean> whereCondition =
        createWhereByAssociation(from, queryRoot, determineJoinColumns());
    whereCondition = addWhereClause(whereCondition,
        createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));

    query.where(whereCondition);
    try {
      createGroupBy(query, queryRoot, association.getJoinColumnsList());
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, association.getAlias());
    }
  }

  /**
   * Example SQL query generated:
   *
   * <pre>
   * SELECT E0."SourceKey" S0, E0."Number" S1
   *     FROM "OLINGO"."JoinSource" E0
   *     WHERE EXISTS (SELECT E2."SourceID" S0
   *         FROM "OLINGO"."JoinRelation" E2
   *         INNER JOIN "OLINGO"."JoinTarget" E3
   *             ON (E2."TargetID" = E3."TargetKey")
   *         WHERE (E2."SourceID" = E0."SourceKey")
   *         GROUP BY E2."SourceID")
   * </pre>
   */
  private void createSubQueryJoinTableNull() throws ODataApplicationException {
    try {
      final List<JPAOnConditionItem> left = association
          .getJoinTable()
          .getJoinColumns(); // Person -->
      final List<JPAOnConditionItem> right = association
          .getJoinTable()
          .getInverseJoinColumns(); // Person -->

      createSelectClauseAggregation(subQuery, queryJoinTable, left, true);
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, left);
      whereCondition = addWhereClause(whereCondition, createWhereByAssociation(queryJoinTable, queryRoot, right));
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          queryRoot));
      subQuery.where(whereCondition);
      createGroupBy(subQuery, queryJoinTable, left);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
