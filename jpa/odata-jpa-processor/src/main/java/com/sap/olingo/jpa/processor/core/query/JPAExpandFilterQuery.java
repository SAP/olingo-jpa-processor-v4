package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.NO_JOIN_TABLE_TYPE;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.VisitableExpression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAIllegalAccessException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterCrossComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterRestrictionsWatchDog;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;
import com.sap.olingo.jpa.processor.core.processor.JPAODataInternalRequestContext;

class JPAExpandFilterQuery extends JPAAbstractSubQuery {
  final List<UriParameter> keyPredicates;
  final JPAODataRequestContextAccess requestContext;
  final JPANavigationPropertyInfo navigationInfo;
  final Optional<JPAAssociationPath> childAssociation;
  final Map<String, From<?, ?>> joinTables;

  JPAExpandFilterQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPANavigationPropertyInfo navigationInfo, final JPAAbstractQuery parent,
      final JPAAssociationPath childAssociation) throws ODataException {

    super(odata,
        requestContext.getEdmProvider().getServiceDocument(),
        (EdmEntityType) navigationInfo.getUriResource().getType(),
        requestContext.getEntityManager(),
        parent,
        null,
        navigationInfo.getAssociationPath(),
        requestContext.getClaimsProvider());
    this.requestContext = requestContext;
    this.keyPredicates = navigationInfo.getKeyPredicates();
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.locale = parent.getLocale();
    this.navigationInfo = navigationInfo;
    this.childAssociation = Optional.ofNullable(childAssociation);
    this.joinTables = new HashMap<>();
    this.debugger = requestContext.getDebugger();
    setFilter(this.navigationInfo);
  }

  public JPAExpandFilterQuery(final OData odata, final JPAODataRequestContextAccess requestContext,
      final JPANavigationPropertyInfo navigationInfo, final JPAAbstractQuery parent,
      final JPAAssociationPath association,
      final JPAAssociationPath childAssociation) throws ODataException {

    super(odata,
        requestContext.getEdmProvider().getServiceDocument(),
        (EdmEntityType) navigationInfo.getUriResource().getType(),
        requestContext.getEntityManager(),
        parent,
        null,
        association,
        requestContext.getClaimsProvider());
    this.requestContext = requestContext;
    this.keyPredicates = navigationInfo.getKeyPredicates();
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.locale = parent.getLocale();
    this.navigationInfo = navigationInfo;
    this.childAssociation = Optional.ofNullable(childAssociation);
    this.joinTables = new HashMap<>();
    this.debugger = requestContext.getDebugger();
    setFilter(this.navigationInfo);
  }

  @SuppressWarnings("unchecked")
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
  public <T> Subquery<T> getSubQuery(@Nullable final Subquery<?> childQuery,
      final VisitableExpression expression, final List<Path<Comparable<?>>> inPath) throws ODataApplicationException {
    // Last childQuery == null
    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createSubQuery")) {
      final ProcessorSubquery<T> nextQuery = (ProcessorSubquery<T>) this.subQuery;
      final JPAQueryPair queries = createQueries(childQuery);
      final List<JPAAssociationPath> orderByAttributes = extractOrderByNavigationAttributes(navigationInfo.getUriInfo()
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
      return nextQuery;
    }
  }

  protected final JPAFilterCrossComplier addFilterCompiler(final JPANavigationPropertyInfo navigationInfo)
      throws ODataJPAModelException, ODataJPAProcessorException, ODataJPAQueryException {

    final JPAOperationConverter converter = new JPAOperationConverter(cb, requestContext.getOperationConverter());
    final JPAODataRequestContextAccess subContext = new JPAODataInternalRequestContext(navigationInfo.getUriInfo(),
        requestContext);

    final JPAFilterRestrictionsWatchDog watchDog = new JPAFilterRestrictionsWatchDog(this.association.getLeaf(),
        !navigationInfo.getKeyPredicates().isEmpty());
    return new JPAFilterCrossComplier(odata, sd, navigationInfo.getEntityType(), converter, this,
        navigationInfo.getFromClause(), null, subContext, watchDog);
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
      final Optional<JPAEntityType> joinTableEt = childAssociation
          .map(JPAAssociationPath::getJoinTable)
          .map(JPAJoinTable::getEntityType);
      joinTableEt.ifPresent(et -> {
        debugger.trace(this, "Join table found: %s, join will be created", joinTableEt.toString());
        queryJoinTable = subQuery.from(et.getTypeClass());
        queryJoinTable.alias(association.getAlias());
        joinTables.put(association.getAlias(), queryJoinTable);
      });
    }
  }

  void setFilter(final JPANavigationPropertyInfo navigationInfo) throws ODataJPAModelException,
      ODataJPAProcessorException,
      ODataJPAQueryException {
    if (navigationInfo.getFilterCompiler() == null)
      navigationInfo.setFilterCompiler(addFilterCompiler(navigationInfo));
  }

  private List<Expression<?>> createGroupBy(final Subquery<?> childQuery,
      final List<JPAAssociationPath> orderByAttributes, final List<JPAPath> selections) {
    if (!orderByAttributes.isEmpty()) {

      return selections.stream()
          .map(path -> mapOnToSelection(path, queryRoot, childQuery))
          .collect(toList()); // NOSONAR
    }
    return emptyList();
  }

  private List<Order> createOrderBy(final Subquery<?> childQuery) throws ODataApplicationException {
    if (!hasRowLimit(childQuery)) {
      final JPAOrderByBuilder orderByBuilder = new JPAOrderByBuilder(jpaEntity, queryRoot, cb, groups);
      return orderByBuilder.createOrderByList(joinTables, navigationInfo.getUriInfo(), navigationInfo.getPage());
    }
    return emptyList();
  }

  JPAQueryPair createQueries(@Nullable final Subquery<?> childQuery) throws ODataApplicationException {
    if (hasRowLimit(navigationInfo) && childQuery != null) {
      debugger.trace(this, "Row number required");

      JPARowNumberFilterQuery rowNumberQuery;
      try {
        rowNumberQuery = new JPARowNumberFilterQuery(odata, requestContext, navigationInfo, parentQuery,
            childAssociation, jpaEntity.getKeyPath());
      } catch (final ODataException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
      return new JPAQueryPair(rowNumberQuery, this);
    } else {
      debugger.trace(this, "Row number not required");
      return new JPAQueryPair(this, this);
    }
  }

  void createRoots(final Subquery<?> childQuery, final JPAQueryPair queries,
      final ProcessorSubquery<?> nextQuery) throws ODataApplicationException {

    if (hasRowLimit(childQuery))
      this.queryRoot = nextQuery.from((ProcessorSubquery<?>) ((JPARowNumberFilterQuery) queries.inner())
          .getSubQuery(childQuery, null, Collections.emptyList()));
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
    if (navigationInfo.getPage() != null)
      return navigationInfo.getPage().skip();
    if (navigationInfo.getUriInfo().getSkipOption() != null && childQuery == null)
      return navigationInfo.getUriInfo().getSkipOption().getValue();
    return null;
  }

  private Integer getTopValue(@Nullable final Subquery<?> childQuery) {
    if (navigationInfo.getPage() != null)
      return navigationInfo.getPage().top();
    if (navigationInfo.getUriInfo().getTopOption() != null && childQuery == null)
      return navigationInfo.getUriInfo().getTopOption().getValue();
    return null;
  }

  private boolean hasRowLimit(@Nullable final Subquery<?> childQuery) {

    return super.hasRowLimit(navigationInfo) && childQuery != null;
  }

  Expression<?> mapOnToSelection(final JPAPath on, final From<?, ?> root, @Nullable final Subquery<?> childQuery) {
    final Path<?> path;
    if (hasRowLimit(childQuery)) {
      path = root.get(on.getAlias());
    } else {
      path = ExpressionUtility.convertToCriteriaPath(root, on.getPath());
    }
    path.alias(on.getLeaf().getInternalName());
    return path;
  }

  private List<Selection<?>> selectIn(final Subquery<?> childQuery, final List<JPAPath> selections) {

    return selections.stream()
        .map(path -> mapOnToSelection(path, queryRoot, childQuery))
        .collect(toList());// NOSONAR
  }

  private List<JPAPath> selectionPathIn() throws ODataJPAQueryException {
    try {
      final List<JPAOnConditionItem> columns = association.hasJoinTable()
          ? association.getJoinTable().getJoinColumns()
          : association.getJoinColumnsList();
      return columns.stream()
          .map(JPAOnConditionItem::getLeftPath)
          .toList();
    } catch (final ODataJPAModelException e) {
      if (e.getId().equals(NO_JOIN_TABLE_TYPE.getKey())) {
        throw new ODataJPAQueryException(QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING, INTERNAL_SERVER_ERROR,
            association.getJoinTable().getTableName());
      }
      throw new ODataJPAQueryException(QUERY_PREPARATION_ERROR, INTERNAL_SERVER_ERROR, e);
    }
  }

  @Override
  public List<Path<Comparable<?>>> getLeftPaths() throws ODataJPAIllegalAccessException {
    return Collections.emptyList();
  }
}
