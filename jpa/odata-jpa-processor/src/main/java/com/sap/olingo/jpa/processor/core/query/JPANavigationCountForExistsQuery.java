package com.sap.olingo.jpa.processor.core.query;

import java.util.Collections;
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
import org.apache.olingo.server.api.uri.UriResourceKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPANavigationCountForExistsQuery extends JPANavigationCountQuery implements ExistsExpressionValue {

  JPANavigationCountForExistsQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType type,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider,
      final List<UriParameter> keyPredicates) throws ODataApplicationException {
    super(odata, sd, type, em, parent, from, association, claimsProvider, keyPredicates);

    this.aggregationType = UriResourceKind.count;
  }

  /**
   * SELECT E2."CodePublisher" S0
   * FROM "OLINGO"."AdministrativeDivision" E2
   * WHERE (((E2."ParentDivisionCode" = E0."DivisionCode")
   * AND (E2."ParentCodeID" = E0."CodeID"))
   * AND (E2."CodePublisher" = E0."CodePublisher"))
   * GROUP BY E2."CodePublisher", E2."ParentCodeID", E2."ParentDivisionCode"
   * HAVING (COUNT(E2."CodePublisher") = 2)
   * @param <T>
   * @param childQuery
   * @param query
   * @throws ODataApplicationException
   */
  @Override
  protected <T> void createSubQueryAggregation(final Subquery<T> query)
      throws ODataApplicationException {

    createSelectClauseJoin(query, queryRoot, determineAggregationRightColumns(), false);
    Expression<Boolean> whereCondition =
        createWhereByAssociation(from, queryRoot, determineJoinColumns());
    whereCondition = addWhereClause(whereCondition,
        createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));

    query.where(applyAdditionalFilter(whereCondition));
    handleAggregation(query, queryRoot, determineAggregationRightColumns());
  }

  @Override
  protected void createSubQueryJoinTableAggregation() throws ODataApplicationException {

//    select distinct E0."SourceKey" S0, E0."Number" S1
//    from "OLINGO"."JoinSource" E0
//    where exists ( select E2."SourceID" S0
//            from  "OLINGO"."JoinRelation" E2
//            inner join "OLINGO"."JoinTarget" E3
//              on (E2."TargetID" = E3."TargetKey")
//            where (E2."SourceID" = E0."SourceKey")
//            group by E2."SourceID"
//            having (COUNT(E3."TargetKey") = 2))
    try {
      final List<JPAOnConditionItem> left = association
          .getJoinTable()
          .getJoinColumns(); // Person -->
      final List<JPAOnConditionItem> right = association
          .getJoinTable()
          .getInverseJoinColumns(); // Person -->

      createSelectClauseAggregation(subQuery, queryJoinTable, left, false);
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, left);
      whereCondition = addWhereClause(whereCondition, createWhereByAssociation(queryJoinTable, queryRoot, right));
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          queryRoot));
      subQuery.where(applyAdditionalFilter(whereCondition));
      createGroupBy(subQuery, queryJoinTable, left);
      createHaving(subQuery);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public List<Path<Comparable<?>>> getLeftPaths() {
    return Collections.emptyList();
  }
}
