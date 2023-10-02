package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.Binary;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPACountExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterExpression;
import com.sap.olingo.jpa.processor.core.filter.JPAMemberOperator;
import com.sap.olingo.jpa.processor.core.filter.JPAVisitableExpression;

public abstract class JPAAbstractSubQuery extends JPAAbstractQuery {

  protected From<?, ?> queryJoinTable = null;
  protected Subquery<?> subQuery;
  protected final JPAAbstractQuery parentQuery;
  protected UriResourceKind aggregationType;
  protected From<?, ?> queryRoot = null;
  protected final From<?, ?> from;
  protected final JPAAssociationPath association;
  protected JPAFilterElementComplier filterComplier;

  JPAAbstractSubQuery(final OData odata, final JPAServiceDocument sd, final EdmEntityType edmEntityType,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider)
      throws ODataApplicationException {

    super(odata, sd, edmEntityType, em, claimsProvider);
    this.parentQuery = parent;
    this.from = from;
    this.association = association;
  }

  JPAAbstractSubQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntity,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association) {

    super(odata, sd, jpaEntity, em, Optional.empty());
    this.parentQuery = parent;
    this.from = from;
    this.association = association;
  }

  public abstract <T extends Object> Subquery<T> getSubQuery(final Subquery<?> childQuery,
      @Nullable VisitableExpression expression) throws ODataApplicationException;

  @SuppressWarnings("unchecked")
  @Override
  public <T> AbstractQuery<T> getQuery() {
    return (AbstractQuery<T>) subQuery;
  }

  @Override
  protected Locale getLocale() {
    return locale;
  }

  @Override
  JPAODataRequestContextAccess getContext() {
    return parentQuery.getContext();
  }

  protected void createRoots(final JPAAssociationPath association) throws ODataJPAQueryException {

    if (association != null && association.hasJoinTable()) {
      if (association.getJoinTable().getEntityType() != null) {
        createJoinTableRoot(association);
      } else {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_IMPLEMENTED,
            HttpStatusCode.NOT_IMPLEMENTED, association.getAlias());
      }
    } else {
      this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
    }
  }

  void createJoinTableRoot(final JPAAssociationPath association) {
    // At least for EclipseLink the order is of importance. First the join table has to be mentioned. It becomes
    // the "leading" table. Otherwise EclipseLink replaces the join table within the WHERE clause.
    this.queryJoinTable = subQuery.from(association.getJoinTable().getEntityType().getTypeClass());
    this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
  }

  @SuppressWarnings("unchecked")
  protected <T> void createSelectClauseJoin(final Subquery<T> subQuery, final From<?, ?> from,
      final List<JPAPath> conditionItems) {

    Path<?> path = from;
    for (final JPAElement jpaPathElement : conditionItems.get(0).getPath())
      path = path.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) path);
  }

  protected Expression<Boolean> createWhereByAssociation(final From<?, ?> subRoot, final From<?, ?> parentFrom,
      final List<JPAOnConditionItem> conditionItems) {

    Expression<Boolean> whereCondition = null;
    for (final JPAOnConditionItem onItem : conditionItems) {
      Path<?> parentPath = parentFrom;
      Path<?> subPath = subRoot;
      for (final JPAElement jpaPathElement : onItem.getRightPath().getPath())
        parentPath = parentPath.get(jpaPathElement.getInternalName());
      for (final JPAElement jpaPathElement : onItem.getLeftPath().getPath())
        subPath = subPath.get(jpaPathElement.getInternalName());
      final Expression<Boolean> equalCondition = cb.equal(parentPath, subPath);
      if (whereCondition == null)
        whereCondition = equalCondition;
      else
        whereCondition = cb.and(whereCondition, equalCondition);
    }
    return whereCondition;

  }

  protected Expression<Boolean> applyAdditionalFilter(final Expression<Boolean> where)
      throws ODataApplicationException {

    Expression<Boolean> whereCondition = where;
    if (filterComplier != null && aggregationType == null)
      try {
        if (filterComplier.getExpressionMember() != null)
          whereCondition = addWhereClause(whereCondition, filterComplier.compile());
      } catch (final ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    return whereCondition;
  }

  /**
   * Self Join
   * @param subRoot
   * @param parentFrom
   * @param jpaEntity
   * @return
   * @throws ODataJPAModelException
   */
  protected Expression<Boolean> createWhereSelfJoin(final From<?, ?> subRoot, final From<?, ?> parentFrom,
      final JPAEntityType jpaEntity) throws ODataJPAModelException {
    Expression<Boolean> whereCondition = null;

    for (final JPAPath onItem : jpaEntity.getKeyPath()) {
      Path<?> parentPath = parentFrom;
      Path<?> subPath = subRoot;
      for (final JPAElement jpaPathElement : onItem.getPath()) {
        parentPath = parentPath.get(jpaPathElement.getInternalName());
        subPath = subPath.get(jpaPathElement.getInternalName());
      }
      final Expression<Boolean> equalCondition = cb.equal(parentPath, subPath);
      whereCondition = addWhereClause(whereCondition, equalCondition);
    }
    return whereCondition;
  }

  @SuppressWarnings("unchecked")
  protected <T> void createSelectClauseAggregation(final Subquery<T> subQuery, final From<?, ?> from,
      final List<JPAOnConditionItem> conditionItems) {
    Path<?> path = from;

    for (final JPAElement jpaPathElement : conditionItems.get(0).getRightPath().getPath())
      path = path.get(jpaPathElement.getInternalName());
    subQuery.select((Expression<T>) path);
  }

  protected void handleAggregation(final Subquery<?> subQuery, final From<?, ?> groupByRoot,
      final List<JPAPath> groupByPath) throws ODataApplicationException {

    final List<Expression<?>> groupByList = new ArrayList<>();
    if (filterComplier != null && this.aggregationType != null) {
      for (final JPAPath onItem : groupByPath) {
        Path<?> subPath = groupByRoot;
        for (final JPAElement jpaPathElement : onItem.getPath())
          subPath = subPath.get(jpaPathElement.getInternalName());
        groupByList.add(subPath);
      }
      subQuery.groupBy(groupByList);

      try {
        subQuery.having(this.filterComplier.compile());
      } catch (final ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    }

  }

  protected UriResourceKind getAggregationType(final VisitableExpression expression) {
    UriInfoResource member = null;
    if (expression instanceof Binary) {
      if (((Binary) expression).getLeftOperand() instanceof JPAMemberOperator)
        member = ((JPAMemberOperator) ((Binary) expression).getLeftOperand()).getMember().getResourcePath();
      else if (((Binary) expression).getRightOperand() instanceof JPAMemberOperator)
        member = ((JPAMemberOperator) ((Binary) expression).getRightOperand()).getMember().getResourcePath();
    } else if (expression instanceof JPAFilterExpression
        || expression instanceof JPACountExpression) {
      member = ((JPAVisitableExpression) expression).getMember();
    }
    if (member != null) {
      for (final UriResource r : member.getUriResourceParts()) {
        if (r.getKind() == UriResourceKind.count)
          return r.getKind();
      }
    }
    return null;
  }

  protected List<JPAPath> determineAggregationRightColumns() throws ODataJPAQueryException {

    try {
      final List<JPAPath> conditionItems = association.hasJoinTable()
          ? association.getJoinTable().getRightColumnsList()
          : association.getRightColumnsList();
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

  protected List<JPAPath> determineAggregationLeftColumns() throws ODataJPAQueryException {

    try {
      final List<JPAPath> conditionItems = association.hasJoinTable()
          ? association.getJoinTable().getLeftColumnsList()
          : association.getLeftColumnsList();
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
