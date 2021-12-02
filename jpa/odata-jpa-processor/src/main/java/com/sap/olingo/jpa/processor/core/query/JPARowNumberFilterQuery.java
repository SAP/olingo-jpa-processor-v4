package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NO_JOIN_TABLE_TYPE;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterComplier;

final class JPARowNumberFilterQuery extends JPAExpandFilterQuery {

  private final JPAFilterComplier filter;
  private final Set<JPAPath> outerSelections;
  private final boolean useInverse;

  JPARowNumberFilterQuery(final OData odata, final JPAODataSessionContextAccess sessionContext,
      final JPAODataRequestContextAccess requestContext, final JPANavigationPropertyInfo naviInfo,
      final JPAAbstractQuery parent, final Optional<JPAAssociationPath> childAssociation,
      final List<JPAPath> selections)
      throws ODataException {

    super(odata, sessionContext, requestContext, new JPANavigationPropertyInfo(naviInfo), parent, childAssociation
        .orElse(null));

    this.outerSelections = selections.stream().collect(toSet());
    this.useInverse = false;
    filter = navigationInfo.getFilterCompiler();
    filter.compile();
  }

  JPARowNumberFilterQuery(final OData odata, final JPAODataSessionContextAccess sessionContext,
      final JPAODataRequestContextAccess requestContext, final JPANavigationPropertyInfo naviInfo,
      final JPAAbstractQuery parent, final JPAAssociationPath association, final JPAAssociationPath childAssociation,
      final SelectionPathInfo<JPAPath> selectionPath)
      throws ODataException {

    super(odata, sessionContext, requestContext, new JPANavigationPropertyInfo(naviInfo), parent, association,
        childAssociation);

    this.outerSelections = selectionPath.joinedPersistent();
    this.useInverse = true;
    filter = navigationInfo.getFilterCompiler();
    filter.compile();
  }

  @Override
  public From<?, ?> getRoot() {
    return queryRoot;
  }

  /**
   *
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  @Override
  public <T> Subquery<T> getSubQuery(@Nullable final Subquery<?> childQuery) throws ODataApplicationException {

    final int handle = debugger.startRuntimeMeasurement(this, "createSubQuery");
    final ProcessorSubquery<T> nextQuery = (ProcessorSubquery<T>) this.subQuery;
    this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
    this.navigationInfo.setFromClause(queryRoot);
    buildJoinTable(emptyList(), outerSelections, null);
    final List<Selection<?>> selections = createSelectForParent();
    selections.addAll(crateSelectionJoinTable());
    selections.add(createRowNumber(useInverse));
    nextQuery.where(createWhereSubQuery(childQuery, useInverse));
    nextQuery.multiselect(selections);
    debugger.stopRuntimeMeasurement(handle);
    return nextQuery;

  }

  private List<? extends Selection<?>> crateSelectionJoinTable() throws ODataJPAQueryException {
    if (queryJoinTable != null) {
      try {
        final List<JPAOnConditionItem> columns = association.getJoinTable().getJoinColumns();
        debugger.trace(this, "Creating SELECT snipped for join table %s with join conditions %s", queryJoinTable
            .toString(), columns);
        return columns
            .stream()
            .map(key -> mapOnToSelection(key.getRightPath(), queryJoinTable, null))
            .collect(toList());
      } catch (final ODataJPAModelException e) {
        if (e.getId().equals(NO_JOIN_TABLE_TYPE.getKey())) {
          throw new ODataJPAQueryException(QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING, INTERNAL_SERVER_ERROR,
              association.getJoinTable().getTableName());
        }
        throw new ODataJPAQueryException(QUERY_PREPARATION_ERROR, INTERNAL_SERVER_ERROR, e);
      }
    }
    return emptyList();
  }

  private List<Selection<?>> createSelectForParent() {

    final int handle = debugger.startRuntimeMeasurement(this, "createSelectClause");
    final List<Selection<?>> selections = new ArrayList<>();

    // Build select clause
    for (final JPAPath jpaPath : this.outerSelections) {
      if (jpaPath.isPartOfGroups(groups)) {
        final Path<?> p = ExpressionUtil.convertToCriteriaPath(joinTables, queryRoot, jpaPath.getPath());
        p.alias(jpaPath.getAlias());
        selections.add(p);
      }
    }
    debugger.stopRuntimeMeasurement(handle);
    return selections;
  }

  private Expression<Long> createRowNumber(final boolean inverse) throws ODataApplicationException {
    try {
      final List<Path<?>> pathList = createWhereKeyInPathList(
          inverse ? association : childAssociation
              .orElseThrow(() -> new ODataJPAQueryException(QUERY_PREPARATION_ERROR, INTERNAL_SERVER_ERROR)),
          queryJoinTable == null ? queryRoot : queryJoinTable);
      final List<Order> orderBy = createOrderBy();
      return (Expression<Long>) ((ProcessorCriteriaBuilder) cb).rowNumber()
          .orderBy(orderBy.isEmpty() ? singletonList(cb.asc(queryRoot)) : orderBy)
          .partitionBy(pathList)
          .alias(ROW_NUMBER_COLUMN_NAME);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

  }

  @Override
  protected Expression<Boolean> applyAdditionalFilter(final Expression<Boolean> where)
      throws ODataApplicationException {

    if (filter != null && aggregationType == null)
      try {
        return addWhereClause(where, filter.compile());
      } catch (final ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    return where;
  }

  private List<Order> createOrderBy() throws ODataApplicationException {
    final JPAOrderByBuilder orderBy = new JPAOrderByBuilder(jpaEntity, queryRoot, cb, groups);
    return orderBy.createOrderByList(emptyMap(), navigationInfo.getUriInfo().getOrderByOption());
  }
}
