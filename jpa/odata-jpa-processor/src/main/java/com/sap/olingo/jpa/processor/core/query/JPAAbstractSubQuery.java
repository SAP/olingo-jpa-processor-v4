package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.AbstractQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;

public abstract class JPAAbstractSubQuery extends JPAAbstractQuery {

  protected From<?, ?> queryJoinTable = null;
  protected Subquery<?> subQuery;
  protected final JPAAbstractQuery parentQuery;
  protected UriResourceKind aggregationType;
  protected From<?, ?> queryRoot = null;
  protected final From<?, ?> from;
  protected final JPAAssociationPath association;
  protected JPAFilterElementComplier filterComplier;
  protected final boolean isCollectionProperty;

  JPAAbstractSubQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntity,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association, final Optional<JPAODataClaimProvider> claimsProvider) {

    super(odata, sd, jpaEntity, em, claimsProvider);
    this.parentQuery = parent;
    this.from = from;
    this.association = association;
    this.isCollectionProperty = toCollectionProperty();
  }

  JPAAbstractSubQuery(final OData odata, final JPAServiceDocument sd, final JPAEntityType jpaEntity,
      final EntityManager em, final JPAAbstractQuery parent, final From<?, ?> from,
      final JPAAssociationPath association) {

    this(odata, sd, jpaEntity, em, parent, from, association, Optional.empty());
  }

  public abstract <T extends Object> Subquery<T> getSubQuery(final Subquery<?> childQuery,
      @Nullable VisitableExpression expression, List<Path<Comparable<?>>> inPath) throws ODataApplicationException;

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
      if (isCollectionProperty) {
        if (association.getTargetType() != null)
          this.queryRoot = subQuery.from(association.getTargetType().getTypeClass());
        else
          this.queryRoot = createCollectionRoot();
      } else if (association.getJoinTable().getEntityType() != null) {
        createJoinTableRoot(association);
      } else {
        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_NOT_IMPLEMENTED,
            HttpStatusCode.NOT_IMPLEMENTED, association.getAlias());
      }
    } else {
      this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
    }
  }

  private From<?, ?> createCollectionRoot() {
    From<?, ?> join = subQuery.from(association.getSourceType().getTypeClass());
    for (final JPAElement element : association.getPath()) {
      join = join.join(element.getInternalName());
      if (element instanceof JPACollectionAttribute) {
        break;
      }
    }
    return join;
  }

  void createJoinTableRoot(final JPAAssociationPath association) {
    // At least for EclipseLink the order is of importance. First the join table has to be mentioned. It becomes
    // the "leading" table. Otherwise EclipseLink replaces the join table within the WHERE clause.
    this.queryJoinTable = subQuery.from(association.getJoinTable().getEntityType().getTypeClass());
    this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
  }

  protected <T> void createSelectClauseAggregation(final Subquery<T> subQuery, final From<?, ?> from,
      final List<JPAOnConditionItem> conditionItems, final boolean forInExpression) {

    final List<Selection<?>> selections = new ArrayList<>();
    for (final JPAOnConditionItem item : conditionItems) {
      selections.add(ExpressionUtility.convertToCriteriaPath(from, item.getRightPath().getPath()));
    }
    setSelection(subQuery, forInExpression, selections);
  }

  protected <T> void createSelectClauseJoin(final Subquery<T> subQuery, final From<?, ?> from,
      final List<JPAPath> conditionItems, final boolean forInExpression) {

    final List<Selection<?>> selections = new ArrayList<>();
    for (final JPAPath item : conditionItems) {
      selections.add(ExpressionUtility.convertToCriteriaPath(from, item.getPath()));
    }
    setSelection(subQuery, forInExpression, selections);
  }

  @SuppressWarnings("unchecked")
  private <T> void setSelection(final Subquery<T> subQuery, final boolean forInExpression,
      final List<Selection<?>> selections) {
    if (forInExpression && selections.size() > 1)
      try {
        ((ProcessorSubquery<T>) subQuery).multiselect(selections);
      } catch (final ClassCastException e) {
        throw new IllegalStateException(
            "IN Clause with multiple attributes only supported with criteria builder extension e.g. odata-jpa-processor-cb");
      }
    else
      subQuery.select((Expression<T>) selections.get(0));
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

  protected void handleAggregation(final Subquery<?> subQuery, final From<?, ?> groupByRoot,
      final List<JPAPath> groupByPath) throws ODataApplicationException {

    final List<Expression<?>> groupByList = new ArrayList<>();
    if (filterComplier != null && this.aggregationType != null) {
      for (final JPAPath onItem : groupByPath) {
        final var subPath = ExpressionUtility.convertToCriteriaPath(groupByRoot, onItem.getPath());
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

  public abstract List<Path<Comparable<?>>> getLeftPaths() throws ODataJPAIllegalAccessException;

  final boolean toCollectionProperty() {
    final var path = association.getPath();
    return path.get(path.size() - 1) instanceof JPACollectionAttribute;
  }
}
