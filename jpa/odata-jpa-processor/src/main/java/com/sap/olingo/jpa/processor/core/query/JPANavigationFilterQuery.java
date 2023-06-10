package com.sap.olingo.jpa.processor.core.query;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADescriptionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;

public final class JPANavigationFilterQuery extends JPAAbstractSubQuery {
  private final List<UriParameter> keyPredicates;

  public JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final From<?, ?> from, final Optional<JPAODataClaimProvider> claimsProvider) throws ODataApplicationException {

    super(odata, sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em, parent, from, association,
        claimsProvider);
    this.keyPredicates = Utility.determineKeyPredicates(uriResourceItem);
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());

    this.locale = parent.getLocale();
    this.filterComplier = null;
    this.aggregationType = null;
    createRoots(association);
  }

  public JPANavigationFilterQuery(final OData odata, final JPAServiceDocument sd, final UriResource uriResourceItem,
      final JPAAbstractQuery parent, final EntityManager em, final JPAAssociationPath association,
      final VisitableExpression expression, final From<?, ?> from,
      final Optional<JPAODataClaimProvider> claimsProvider, final List<String> groups)
      throws ODataApplicationException {

    super(odata, sd, (EdmEntityType) ((UriResourcePartTyped) uriResourceItem).getType(), em, parent, from,
        association, claimsProvider);
    this.keyPredicates = Utility.determineKeyPredicates(uriResourceItem);
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());

    this.locale = parent.getLocale();

    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity, new JPAOperationConverter(cb,
        getContext().getOperationConverter()), null, this, expression, null, groups);
    this.aggregationType = getAggregationType(this.filterComplier.getExpressionMember());
    createRoots(association);
    createDescriptionJoin();
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.query.JPANavigationQuery#getRoot()
   */
  @Override
  public From<?, ?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

  /**
   * Creates a exist sub query including the where clause joining this query with the parent query
   */
  @Override
  @SuppressWarnings("unchecked")
  public <T extends Object> Subquery<T> getSubQuery(final Subquery<?> childQuery)
      throws ODataApplicationException {

    final Subquery<T> query = (Subquery<T>) this.subQuery;
    if (this.association != null && this.association.getJoinTable() != null) {
      if (this.aggregationType != null)
        createSubQueryJoinTableAggregation();
      else
        createSubQueryJoinTable();
    } else {
      if (this.aggregationType != null)
        createSubQueryAggregation(childQuery, query);
      else
        createSubQuery(childQuery, query);
    }
    return query;
  }

  private void createDescriptionJoin() throws ODataApplicationException {
    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    generateDescriptionJoin(joinTables, determineAllDescriptionPath(), getRoot());
  }

  private <T> void createSubQueryAggregation(final Subquery<?> childQuery, final Subquery<T> query)
      throws ODataApplicationException {

    try {
      createSelectClauseJoin(query, queryJoinTable, determineAggregationLeftColumns());
      Expression<Boolean> whereCondition = createWhereSelfJoin(from, queryJoinTable, parentQuery.getJpaEntity());
      if (childQuery != null) {
        whereCondition = cb.and(whereCondition, cb.exists(childQuery));
      }
      whereCondition = addWhereClause(whereCondition,
          createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));

      query.where(applyAdditionalFilter(whereCondition));
      handleAggregation(query, queryJoinTable, determineAggregationLeftColumns());
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private <T> void createSubQuery(final Subquery<?> childQuery, final Subquery<T> query)
      throws ODataApplicationException {

    createSelectClauseJoin(query, queryRoot, determineAggregationRightColumns());
    Expression<Boolean> whereCondition = null;
    whereCondition = addWhereClause(
        createWhereByAssociation(from, queryRoot, determineJoinColumns()),
        createWhereByKey(queryRoot, this.keyPredicates, jpaEntity));
    if (childQuery != null) {
      whereCondition = cb.and(whereCondition, cb.exists(childQuery));
    }
    whereCondition = addWhereClause(whereCondition,
        createProtectionWhereForEntityType(claimsProvider, jpaEntity, queryRoot));

    query.where(applyAdditionalFilter(whereCondition));
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
      createSelectClauseJoin(subQuery, queryRoot, determineAggregationLeftColumns());
      Expression<Boolean> whereCondition = createWhereByAssociation(from, queryJoinTable, left);
      whereCondition = cb.and(whereCondition, createWhereByAssociation(queryJoinTable, queryRoot, right));
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          queryRoot));
      subQuery.where(applyAdditionalFilter(whereCondition));
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private void createSubQueryJoinTableAggregation() throws ODataApplicationException {
    /*
     * SELECT t0."ID"
     * FROM "OLINGO"."BusinessPartner" t0
     * WHERE (EXISTS (SELECT t1."ID"
     * -- Left Outer Join needed. Otherwise comparisons with 0 in the Having clause wont work
     * FROM "OLINGO"."BusinessPartner" t1
     * LEFT OUTER JOIN ("OLINGO"."Membership" t3 JOIN "OLINGO"."Team" t2
     * ON (t2."TeamKey" = t3."TeamID"))
     * ON (t3."PersonID" = t1."ID")
     * WHERE ((t1."ID" = t0."ID")
     * AND (t1."Type" = '1'))
     * GROUP BY t1."ID"
     * HAVING (COUNT(t2."TeamKey") > 0))
     * AND (t0."Type" = '1'))
     */
    try {
      final List<JPAOnConditionItem> left = association
          .getJoinTable()
          .getJoinColumns(); // Person -->

      createSelectClauseAggregation(subQuery, queryJoinTable, left);
      Expression<Boolean> whereCondition = createWhereSelfJoin(from, queryJoinTable, parentQuery.getJpaEntity());
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          queryRoot));
      subQuery.where(applyAdditionalFilter(whereCondition));
      handleAggregation(subQuery, queryJoinTable, determineAggregationLeftColumns());
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  private Set<JPAPath> determineAllDescriptionPath() throws ODataApplicationException {
    final Set<JPAPath> allPath = new HashSet<>();
    if (filterComplier != null) {
      for (final JPAPath path : filterComplier.getMember()) {
        if (path.getLeaf() instanceof JPADescriptionAttribute)
          allPath.add(path);
      }
    }
    return allPath;
  }

  private List<JPAOnConditionItem> determineJoinColumns() throws ODataJPAQueryException {

    try {
      final List<JPAOnConditionItem> conditionItems = association.getJoinColumnsList();
      if (conditionItems.isEmpty())
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_NOT_DEFINED,
            HttpStatusCode.INTERNAL_SERVER_ERROR, association.getTargetType().getExternalName(), association
                .getSourceType().getExternalName());
      return conditionItems;

    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e, association.getAlias());
    }
  }
}
