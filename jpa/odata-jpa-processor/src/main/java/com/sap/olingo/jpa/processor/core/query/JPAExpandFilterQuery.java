package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NO_JOIN_TABLE_TYPE;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterCrossComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;

class JPAExpandFilterQuery extends JPAAbstractSubQuery {
  final List<UriParameter> keyPredicates;
  final JPAODataRequestContextAccess requestContext;
  final JPANavigationPropertyInfo navigationInfo;
  final Optional<JPAAssociationPath> childAssociation;
  final Map<String, From<?, ?>> joinTables;

  JPAExpandFilterQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPANavigationPropertyInfo naviInfo, final JPAAbstractQuery parent,
      final JPAAssociationPath childAssociation) throws ODataException {

    super(odata,
        requestContext.getEdmProvider().getServiceDocument(),
        (EdmEntityType) naviInfo.getUriResource().getType(),
        requestContext.getEntityManager(),
        parent,
        null,
        naviInfo.getAssociationPath(),
        requestContext.getClaimsProvider());
    this.requestContext = requestContext;
    this.keyPredicates = naviInfo.getKeyPredicates();
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.locale = parent.getLocale();
    this.navigationInfo = naviInfo;
    this.childAssociation = Optional.ofNullable(childAssociation);
    this.joinTables = new HashMap<>();
    this.debugger = requestContext.getDebugger();
    setFilter(navigationInfo);
  }

  public JPAExpandFilterQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPANavigationPropertyInfo naviInfo, final JPAAbstractQuery parent, final JPAAssociationPath association,
      final JPAAssociationPath childAssociation) throws ODataException {

    super(odata,
        requestContext.getEdmProvider().getServiceDocument(),
        (EdmEntityType) naviInfo.getUriResource().getType(),
        requestContext.getEntityManager(),
        parent,
        null,
        association,
        requestContext.getClaimsProvider());
    this.requestContext = requestContext;
    this.keyPredicates = naviInfo.getKeyPredicates();
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.locale = parent.getLocale();
    this.navigationInfo = naviInfo;
    this.childAssociation = Optional.ofNullable(childAssociation);
    this.joinTables = new HashMap<>();
    this.debugger = requestContext.getDebugger();
    setFilter(navigationInfo);
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
    // Last childQuery == null
    final int handle = debugger.startRuntimeMeasurement(this, "createSubQuery");
    final ProcessorSubquery<T> nextQuery = (ProcessorSubquery<T>) this.subQuery;
    final JPAQueryPair queries = createQueries(childQuery);
    final List<JPAAssociationPath> orderByAttributes = extractOrderByNaviAttributes(navigationInfo.getUriInfo()
        .getOrderByOption());
    createRoots(childQuery, queries, nextQuery);
    buildJoinTable(orderByAttributes, emptyList(), childQuery);
    final List<JPAPath> selections = selectionPathIn();
    nextQuery.where(createWhere(childQuery));
    nextQuery.multiselect(selectIn(childQuery, selections));
    nextQuery.orderBy(createOrderBy(childQuery));
    nextQuery.setFirstResult(getSkipValue(childQuery));
    nextQuery.setMaxResults(getTopValue(childQuery));
    nextQuery.groupBy(createGroupBy(childQuery, orderByAttributes, selections));
    debugger.stopRuntimeMeasurement(handle);
    return nextQuery;
  }

  protected final JPAFilterCrossComplier addFilterCompiler(final JPANavigationPropertyInfo naviInfo)
      throws ODataJPAModelException, ODataJPAProcessorException {

    final JPAOperationConverter converter = new JPAOperationConverter(cb, requestContext.getOperationConverter());
    final JPAODataRequestContextAccess subContext = new JPAODataInternalRequestContext(naviInfo.getUriInfo(),
        requestContext);
    return new JPAFilterCrossComplier(odata, sd, naviInfo.getEntityType(), converter, this,
        naviInfo.getFromClause(), null, subContext);
  }

  @Override
  protected Expression<Boolean> applyAdditionalFilter(final Expression<Boolean> where)
      throws ODataApplicationException {

    if (navigationInfo.getFilterCompiler() != null && aggregationType == null)
      try {
        return addWhereClause(where, navigationInfo.getFilterCompiler().compile());
      } catch (final ExpressionVisitException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    return where;
  }

  void buildJoinTable(final List<JPAAssociationPath> orderByAttributes, final Collection<JPAPath> selectionPath,
      final Subquery<?> childQuery)
      throws ODataApplicationException {
    createFromClauseJoinTable(joinTables, childQuery);
    createFromClauseOrderBy(orderByAttributes, joinTables, queryRoot);
    createFromClauseDescriptionFields(selectionPath, joinTables, queryRoot, singletonList(navigationInfo));
  }

  private void createFromClauseJoinTable(final Map<String, From<?, ?>> joinTables, final Subquery<?> childQuery) {
    if (!hasRowLimit(childQuery)) {
      final Optional<JPAEntityType> jtEt = childAssociation
          .map(JPAAssociationPath::getJoinTable)
          .map(JPAJoinTable::getEntityType);
      jtEt.ifPresent(et -> {
        debugger.trace(this, "Join table found: %s, join will be created", jtEt.toString());
        queryJoinTable = subQuery.from(et.getTypeClass());
        queryJoinTable.alias(association.getAlias());
        joinTables.put(association.getAlias(), queryJoinTable);
      });
    }
  }

  void setFilter(final JPANavigationPropertyInfo naviInfo) throws ODataJPAModelException, ODataJPAProcessorException {
    if (naviInfo.getFilterCompiler() == null)
      naviInfo.setFilterCompiler(addFilterCompiler(naviInfo));
  }

  private List<Expression<?>> createGroupBy(final Subquery<?> childQuery,
      final List<JPAAssociationPath> orderByAttributes, final List<JPAPath> selections) {
    if (!orderByAttributes.isEmpty()) {

      return selections.stream()
          .map(p -> mapOnToSelection(p, queryRoot, childQuery))
          .collect(toList());
    }
    return emptyList();
  }

  private List<Order> createOrderBy(final Subquery<?> childQuery) throws ODataApplicationException {
    if (!hasRowLimit(childQuery)) {
      final JPAOrderByBuilder orderByBuilder = new JPAOrderByBuilder(jpaEntity, queryRoot, cb, groups);
      return orderByBuilder.createOrderByList(joinTables, navigationInfo.getUriInfo(), (JPAODataPage)null);
    }
    return emptyList();
  }

  JPAQueryPair createQueries(@Nullable final Subquery<?> childQuery) throws ODataApplicationException {
    if (hasRowLimit(navigationInfo) && childQuery != null) {
      debugger.trace(this, "Row number required");

      JPARowNumberFilterQuery rq;
      try {
        rq = new JPARowNumberFilterQuery(odata, requestContext, navigationInfo, parentQuery, childAssociation, jpaEntity
            .getKeyPath());
      } catch (final ODataException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
      return new JPAQueryPair(rq, this);
    } else {
      debugger.trace(this, "Row number not required");
      return new JPAQueryPair(this, this);
    }
  }

  void createRoots(final Subquery<?> childQuery, final JPAQueryPair queries,
      final ProcessorSubquery<?> nextQuery) throws ODataApplicationException {

    if (hasRowLimit(childQuery))
      this.queryRoot = nextQuery.from((ProcessorSubquery<?>) ((JPARowNumberFilterQuery) queries.getInner()).getSubQuery(
          childQuery));
    else
      this.queryRoot = subQuery.from(this.jpaEntity.getTypeClass());
    navigationInfo.setFromClause(queryRoot);
  }

  private Expression<Boolean> createWhere(final Subquery<?> childQuery) throws ODataApplicationException {

    if (hasRowLimit(childQuery))
      return createWhereByRowNumber(queryRoot, navigationInfo);

    return createWhereSubQuery(childQuery, false);
  }

  Expression<Boolean> createWhereSubQuery(@Nullable final Subquery<?> childQuery, final boolean useInverse)
      throws ODataApplicationException {
    Expression<Boolean> whereCondition = createWhereByKey(queryRoot, this.keyPredicates, jpaEntity);
    whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
        queryRoot));
    if (queryJoinTable != null)
      whereCondition = addWhereClause(whereCondition, createWhereTableJoin(queryJoinTable, queryRoot, association,
          useInverse));

    if (childQuery != null) {
      whereCondition = addWhereClause(whereCondition,
          createWhereKeyIn(childAssociation
              .orElseThrow(() -> new ODataJPAQueryException(QUERY_PREPARATION_ERROR, INTERNAL_SERVER_ERROR)),
              queryJoinTable == null ? queryRoot : queryJoinTable, childQuery));
    }
    return applyAdditionalFilter(whereCondition);
  }

  private Integer getSkipValue(@Nullable final Subquery<?> childQuery) {
    if (navigationInfo.getUriInfo().getSkipOption() != null && childQuery == null)
      return navigationInfo.getUriInfo().getSkipOption().getValue();
    else
      return null;
  }

  private Integer getTopValue(@Nullable final Subquery<?> childQuery) {
    if (navigationInfo.getUriInfo().getTopOption() != null && childQuery == null)
      return navigationInfo.getUriInfo().getTopOption().getValue();
    else
      return null;
  }

  private boolean hasRowLimit(@Nullable final Subquery<?> childQuery) {

    return super.hasRowLimit(navigationInfo) && childQuery != null;
  }

  Expression<?> mapOnToSelection(final JPAPath on, final From<?, ?> root, @Nullable final Subquery<?> childQuery) {
    final Path<?> p;
    if (hasRowLimit(childQuery)) {
      p = root.get(on.getAlias());
    } else {
      p = ExpressionUtil.convertToCriteriaPath(root, on.getPath());
    }
    p.alias(on.getLeaf().getInternalName());
    return p;
  }

  private List<Selection<?>> selectIn(final Subquery<?> childQuery, final List<JPAPath> selections) {

    return selections.stream()
        .map(p -> mapOnToSelection(p, queryRoot, childQuery))
        .collect(toList());
  }

  private List<JPAPath> selectionPathIn() throws ODataJPAQueryException {
    try {
      final List<JPAOnConditionItem> columns = association.hasJoinTable()
          ? association.getJoinTable().getJoinColumns()
          : association.getJoinColumnsList();
      return columns.stream()
          .map(JPAOnConditionItem::getLeftPath)
          .collect(toList());
    } catch (final ODataJPAModelException e) {
      if (e.getId().equals(NO_JOIN_TABLE_TYPE.getKey())) {
        throw new ODataJPAQueryException(QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING, INTERNAL_SERVER_ERROR,
            association.getJoinTable().getTableName());
      }
      throw new ODataJPAQueryException(QUERY_PREPARATION_ERROR, INTERNAL_SERVER_ERROR, e);
    }
  }
}
