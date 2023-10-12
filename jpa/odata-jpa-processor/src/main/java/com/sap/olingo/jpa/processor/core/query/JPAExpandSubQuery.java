package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.olingo.commons.api.http.HttpStatusCode.BAD_REQUEST;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Selection;
import jakarta.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJoinTable;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * Requires Processor Query
 *
 * @author Oliver Grande
 * @since 1.0.1
 * 25.11.2020
 */
public class JPAExpandSubQuery extends JPAAbstractExpandQuery {

  public JPAExpandSubQuery(final OData odata, final JPAInlineItemInfo item,
      final JPAODataRequestContextAccess requestContext) throws ODataException {

    super(odata, requestContext, item);
  }

  @Override
  public JPAExpandQueryResult execute() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "execute")) {
      final JPAQueryCreationResult tupleQuery = createTupleQuery();
      final List<Tuple> intermediateResult = tupleQuery.query().getResultList();
      final Map<String, List<Tuple>> result = convertResult(intermediateResult);
      return new JPAExpandQueryResult(result, count(), jpaEntity, tupleQuery.selection().joinedRequested());
    } catch (final JPANoSelectionException e) {
      return new JPAExpandQueryResult(emptyMap(), emptyMap(), this.jpaEntity, emptyList());
    } catch (final ODataApplicationException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), INTERNAL_SERVER_ERROR.getStatusCode(), getLocale(),
          e);
    }
  }

  @Override
  protected Map<String, From<?, ?>> createFromClause(final List<JPAAssociationPath> orderByTarget,
      final Collection<JPAPath> selectionPath, final CriteriaQuery<?> query, final JPANavigationPropertyInfo lastInfo)
      throws ODataApplicationException, JPANoSelectionException {

    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();
    debugger.trace(this, "Create FROM clause for %s", query.toString());
    createFromClauseRoot(query, joinTables, lastInfo);
    target = root;
    createFromClauseJoinTable(joinTables);
    lastInfo.setFromClause(target);
    createFromClauseDescriptionFields(selectionPath, joinTables, target, singletonList(lastInfo));
    return joinTables;
  }

  private List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables, // NOSONAR
      final Collection<JPAPath> requestedProperties, final List<String> groups)
      throws ODataApplicationException {

    final List<Selection<?>> selections;
    if (hasRowLimit(lastInfo)) {
      selections = new ArrayList<>(requestedProperties.size());
      for (final JPAPath jpaPath : requestedProperties) {
        if (jpaPath.isPartOfGroups(groups)) {
          final Path<?> path = target.get(jpaPath.getAlias());
          path.alias(jpaPath.getAlias());
          selections.add(path);
        }
      }
    } else {
      selections = super.createSelectClause(joinTables, requestedProperties, target, groups);
    }
    addSelectJoinTable(selections);
    debugger.trace(this, "Determined selections %s", selections.toString());
    return selections;
  }

  void addSelectJoinTable(final List<Selection<?>> selections) throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      try {
        final JPAJoinTable joinTable = association.getJoinTable();
        debugger.trace(this, "Creating SELECT snipped for join table %s with join conditions %s", joinTable.toString(),
            joinTable.getJoinColumns());
        for (final JPAOnConditionItem jc : association.getJoinTable().getJoinColumns()) {
          final Path<?> path = root.get(jc.getRightPath().getLeaf().getInternalName());
          path.alias(association.getAlias() + ALIAS_SEPARATOR + jc.getLeftPath().getAlias());
          selections.add(path);
        }
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
    }
  }

  @Override
  protected JPAAssociationPath getAssociation(final JPAInlineItemInfo item) {
    return item.hops.get(item.hops.size() - 2).getAssociationPath();
  }

  LinkedList<JPAAbstractQuery> buildSubQueries(final JPAQueryPair queries) throws ODataException {
    final LinkedList<JPAAbstractQuery> hops = new LinkedList<>();
    hops.push(queries.inner());
    for (int i = navigationInfo.size() - 2; i >= 0; i--) {
      final JPANavigationPropertyInfo hop = navigationInfo.get(i);
      if (hop.getUriInfo() != null) {
        final JPAAbstractQuery parent = hops.getLast();
        final JPAAssociationPath childAssociation = i > 0 ? navigationInfo.get(i - 1).getAssociationPath() : null;
        hops.push(new JPAExpandFilterQuery(odata, requestContext, hop, parent, childAssociation));
        debugger.trace(this, "Sub query created: %s for %s", hops.getFirst().getQuery(), hops.getFirst().jpaEntity);
      }
    }
    return hops;
  }

  @Override
  final Map<String, Long> count() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "count")) {
      final JPAExpandSubCountQuery countQuery = new JPAExpandSubCountQuery(odata, requestContext, jpaEntity,
          association, navigationInfo);
      return countQuery.count();
    } catch (final ODataException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
  }

  Subquery<Object> linkSubQueries(final LinkedList<JPAAbstractQuery> hops) throws ODataApplicationException {
    Subquery<Object> subQuery = null;
    while (!hops.isEmpty() && hops.getFirst() instanceof JPAAbstractSubQuery) {
      final JPAAbstractSubQuery hop = (JPAAbstractSubQuery) hops.pop();
      subQuery = hop.getSubQuery(subQuery, null);
    }
    return subQuery;
  }

  private Map<String, List<Tuple>> convertResult(final List<Tuple> intermediateResult)
      throws ODataApplicationException {
    String joinKey = "";
    List<Tuple> subResult = null;
    final Map<String, List<Tuple>> convertedResult = new HashMap<>();
    for (final Tuple row : intermediateResult) {
      String actualKey;
      try {
        actualKey = buildConcatenatedKey(row, association);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, BAD_REQUEST);
      }
      if (!actualKey.equals(joinKey)) {
        subResult = new ArrayList<>();
        convertedResult.put(actualKey, subResult);
        joinKey = actualKey;
      }
      if (subResult != null) {
        subResult.add(row);
      }
    }
    return convertedResult;
  }

  private Expression<Boolean> createExpandWhere(final JPANavigationPropertyInfo navigationInfo)
      throws ODataApplicationException {

    try {
      return navigationInfo.getFilterCompiler().compile();
    } catch (final ExpressionVisitException e) {
      throw new ODataJPAQueryException(QUERY_PREPARATION_FILTER_ERROR, HttpStatusCode.BAD_REQUEST, e);
    }
  }

  private void createFromClauseJoinTable(final HashMap<String, From<?, ?>> joinTables) throws ODataJPAQueryException {
    if (association.hasJoinTable()) {
      final JPAJoinTable joinTable = association.getJoinTable();
      final JPAEntityType joinTableEt = Optional.ofNullable(joinTable.getEntityType())
          .orElseThrow(() -> new ODataJPAQueryException(QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING,
              INTERNAL_SERVER_ERROR, joinTable.getTableName()));
      debugger.trace(this, "Join table found: %s, join will be created", joinTableEt.toString());
      root = cq.from(joinTableEt.getTypeClass());
      root.alias(association.getAlias());
      joinTables.put(association.getAlias(), target);
    }
  }

  private void createFromClauseRoot(final CriteriaQuery<?> query, final HashMap<String, From<?, ?>> joinTables,
      final JPANavigationPropertyInfo lastInfo) throws ODataJPAQueryException {
    try {
      final JPAEntityType sourceEt = lastInfo.getEntityType();
      this.root = query.from(sourceEt.getTypeClass());
      joinTables.put(sourceEt.getExternalFQN().getFullQualifiedNameAsString(), root);
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
  }

  private List<Order> createOrderBy(final Map<String, From<?, ?>> joinTables) throws ODataApplicationException {
    if (association.hasJoinTable() && hasRowLimit(lastInfo)) {
      try {
        final List<Order> orders = new ArrayList<>();

        for (final JPAOnConditionItem c : association.getJoinTable().getJoinColumns()) {
          final Path<?> path = root.get(c.getLeftPath().getAlias());
          orders.add(cb.asc(path));
        }
        return orders;
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
      }
    } else if (hasRowLimit(lastInfo)) {
      final JPAOrderByBuilder orderByBuilder = new JPAOrderByBuilder(jpaEntity, root, cb, groups);
      return orderByBuilder.createOrderByListAlias(joinTables, uriResource.getOrderByOption(), association);
    } else {
      final JPAOrderByBuilder orderByBuilder = new JPAOrderByBuilder(jpaEntity, root, cb, groups);
      return orderByBuilder.createOrderByList(joinTables, uriResource.getOrderByOption(), association);
    }
  }

  /**
   * Create top level expand query including an inner query with a row number window function in case this is necessary
   * @param selectionPath
   * @return
   * @throws ODataException
   */
  private JPAQueryPair createQueries(final SelectionPathInfo<JPAPath> selectionPath) throws ODataException {
    if (hasRowLimit(lastInfo)) {
      debugger.trace(this, "Row number required");
      final int lastIndex = navigationInfo.size() - 2;
      final JPAAssociationPath childAssociation = navigationInfo.get(lastIndex).getAssociationPath();
      final JPARowNumberFilterQuery rowNumberQuery = new JPARowNumberFilterQuery(odata, requestContext, lastInfo,
          this, association, childAssociation, selectionPath);
      return new JPAQueryPair(rowNumberQuery, this);
    } else {
      debugger.trace(this, "Row number not required");
      return new JPAQueryPair(this, this);
    }
  }

  private @Nonnull JPAQueryCreationResult createTupleQuery() throws JPANoSelectionException,
      ODataException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createTupleQuery")) {
      final ProcessorCriteriaQuery<Tuple> tupleQuery = (ProcessorCriteriaQuery<Tuple>) cq;
      final List<JPAAssociationPath> orderByAttributes = extractOrderByNavigationAttributes(uriResource
          .getOrderByOption());
      final SelectionPathInfo<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
      final JPAQueryPair queries = createQueries(selectionPath);
      addFilterCompiler(lastInfo);
      final LinkedList<JPAAbstractQuery> hops = buildSubQueries(queries);
      final Subquery<Object> subQuery = linkSubQueries(hops);
      final Map<String, From<?, ?>> joinTables = createJoinTables(tupleQuery, selectionPath, orderByAttributes,
          subQuery);
      tupleQuery.where(createWhere(subQuery, lastInfo));
      tupleQuery.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), groups));
      tupleQuery.orderBy(createOrderBy(joinTables));
      tupleQuery.distinct(orderByAttributes.isEmpty());
      if (!orderByAttributes.isEmpty())
        cq.groupBy(createGroupBy(joinTables, target, selectionPath.joinedPersistent()));
      final TypedQuery<Tuple> query = em.createQuery(tupleQuery);
      return new JPAQueryCreationResult(query, selectionPath);
    }
  }

  Map<String, From<?, ?>> createJoinTables(final ProcessorCriteriaQuery<Tuple> tupleQuery,
      final SelectionPathInfo<JPAPath> selectionPath, final List<JPAAssociationPath> orderByAttributes,
      final Subquery<Object> subQuery) throws ODataApplicationException, JPANoSelectionException {

    Map<String, From<?, ?>> joinTables = new HashMap<>();

    if (hasRowLimit(lastInfo)) {
      this.target = this.root = tupleQuery.from((ProcessorSubquery<?>) subQuery);
    } else {
      joinTables = createFromClause(emptyList(), selectionPath.joinedPersistent(), cq, lastInfo);
    }
    createFromClauseOrderBy(orderByAttributes, joinTables, root);
    return joinTables;
  }

  private Expression<Boolean> createWhere(final Subquery<?> subQuery, final JPANavigationPropertyInfo navigationInfo)
      throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createWhere")) {
      if (hasRowLimit(lastInfo)) {
        return createWhereByRowNumber(target, lastInfo);
      }
      jakarta.persistence.criteria.Expression<Boolean> whereCondition = null;
      // Given keys: Organizations('1')/Roles(...)
      whereCondition = createWhereByKey(navigationInfo);
      whereCondition = addWhereClause(whereCondition, createWhereTableJoin(root, target, association, true));
      whereCondition = addWhereClause(whereCondition, createWhereKeyIn(this.association, root, subQuery));
      whereCondition = addWhereClause(whereCondition, createExpandWhere(navigationInfo));
      whereCondition = addWhereClause(whereCondition, createProtectionWhereForEntityType(claimsProvider, jpaEntity,
          target));
      return whereCondition;
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }
  }
}
