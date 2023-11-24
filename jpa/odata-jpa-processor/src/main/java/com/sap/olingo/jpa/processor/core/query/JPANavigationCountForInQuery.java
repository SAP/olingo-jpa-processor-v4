package com.sap.olingo.jpa.processor.core.query;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPANavigationCountForInQuery extends JPANavigationCountQuery implements InExpressionValue {
  private Optional<List<Path<Comparable<?>>>> leftPaths;

  JPANavigationCountForInQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType type,
      final EntityManager em,
      final JPAAbstractQuery parent, final From<?, ?> from, final JPAAssociationPath association,
      final Optional<JPAODataClaimProvider> claimsProvider, final List<UriParameter> keyPredicates)
      throws ODataApplicationException {
    super(odata, sd, type, em, parent, from, association, claimsProvider, keyPredicates);
    leftPaths = Optional.empty();
  }

  /**
   * <pre>
   * WHERE (t0."CodePublisher", t0."CodeID", t0."DivisionCode") IN (
   *    SELECT t1."CodePublisher",t1."ParentCodeID", t1."ParentDivisionCode"
   *    FROM "OLINGO"."AdministrativeDivision" t1
   *    GROUP BY t1."CodePublisher", t1."ParentCodeID", t1."ParentDivisionCode"
   *    HAVING (COUNT(t1."DivisionCode") >= 1) )
   * </pre>
   */
  @Override
  protected <T> void createSubQueryAggregation(final Subquery<T> query) throws ODataApplicationException {

    createSelectClauseJoin(query, queryRoot, determineAggregationRightColumns(), true);
    leftPaths = Optional.of(ExpressionUtility.convertToCriteriaPaths(from, determineAggregationLeftColumns()));
    final Expression<Boolean> whereCondition = createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot);
    query.where(applyAdditionalFilter(whereCondition));
    handleAggregation(query, queryRoot, determineAggregationRightColumns());
  }

  /**
   * <pre>
   * WHERE ((t0."ID") IN (
   *    SELECT t1."SourceID"
   *    FROM "OLINGO"."BusinessPartnerRole" t2,
   *        "OLINGO"."JoinPartnerRoleRelation" t1
   *    WHERE (((t2."BusinessPartnerID" = t1."SourceID")
   *    AND (t2."BusinessPartnerRole" = t1."TargetID"))
   *    AND (t2."BusinessPartnerRole" = 'B'))
   *    GROUP BY t1."SourceID"
   *    HAVING (COUNT(t1."SourceID") = 1))
   * </pre>
   */
  @Override
  protected void createSubQueryJoinTableAggregation() throws ODataApplicationException {
    try {
      final List<JPAOnConditionItem> left = association
          .getJoinTable()
          .getJoinColumns(); // Person -->
      final List<JPAOnConditionItem> right = association
          .getJoinTable()
          .getInverseJoinColumns(); // Person -->

      createSelectClauseAggregation(subQuery, queryJoinTable, left, true);
      leftPaths = Optional.of(buildLeftPath(from, left));
      Expression<Boolean> whereCondition = createWhereByAssociation(queryJoinTable, queryRoot, right);
      whereCondition = addWhereClause(whereCondition,
          createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));
      subQuery.where(applyAdditionalFilter(whereCondition));
      createGroupBy(subQuery, queryJoinTable, left);
      createHaving(subQuery);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private List<Path<Comparable<?>>> buildLeftPath(final From<?, ?> from,
      final List<JPAOnConditionItem> conditionItems) {
    return conditionItems.stream()
        .map(item -> ExpressionUtility.<Comparable<?>> convertToCriteriaPath(from, item.getLeftPath().getPath()))
        .toList();
  }

  @Override
  public List<Path<Comparable<?>>> getLeftPaths() throws ODataJPAIllegalAccessException {
    return leftPaths.orElseThrow(ODataJPAIllegalAccessException::new);
  }

}
