package com.sap.olingo.jpa.processor.core.query;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
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
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.ProcessorSubquery;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;

/**
 * Requires Processor Query
 *
 * @author Oliver Grande
 * @since 1.0.1
 * 25.11.2020
 */
public class JPAExpandSubQuery extends JPAAbstractExpandSubQuery implements JPAExpandQuery {

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
      return new JPAExpandQueryResult(result, count(), jpaEntity, tupleQuery.selection().joinedRequested(),
          skipTokenProvider);
    } catch (final JPANoSelectionException e) {
      return new JPAExpandQueryResult(emptyMap(), emptyMap(), this.jpaEntity, emptyList(), Optional.empty());
    } catch (final ODataApplicationException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), INTERNAL_SERVER_ERROR.getStatusCode(), getLocale(),
          e);
    }
  }

  private List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables, // NOSONAR
      final Collection<JPAPath> requestedProperties, final List<String> groups)
      throws ODataApplicationException {

    List<Selection<?>> selections = List.of();
    try {
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
      selections = addSelectJoinTable(selections);
      return selections;
    } finally {
      debugger.trace(this, "Determined selections %s", selections.toString());
    }
  }

  @Override
  final Map<String, Long> count() throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "count")) {
      if (countRequested(lastInfo)) {
        final JPAExpandSubCountQuery countQuery = new JPAExpandSubCountQuery(odata, requestContext, jpaEntity,
            association, navigationInfo);
        return countQuery.count();
      }
      return emptyMap();
    } catch (final ODataException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
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

  private List<Order> createOrderBy(final Map<String, From<?, ?>> joinTables,
      final List<JPAProcessorAttribute> orderByAttributes) throws ODataApplicationException {

    final JPAOrderByBuilder orderByBuilder = new JPAOrderByBuilder(jpaEntity, root, cb, groups);
    if (association.hasJoinTable() && hasRowLimit(lastInfo)) {
      return orderByBuilder.createOrderByList(association);
    } else if (hasRowLimit(lastInfo)) {
      return orderByBuilder.createOrderByListAlias(joinTables, orderByAttributes, association);
    } else {
      return orderByBuilder.createOrderByList(joinTables, orderByAttributes, association);
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
      final var orderByAttributes = getOrderByAttributes(uriResource.getOrderByOption());
      final SelectionPathInfo<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
      final JPAQueryPair queries = createQueries(selectionPath);
      addFilterCompiler(lastInfo);
      final LinkedList<JPAAbstractQuery> hops = buildSubQueries(queries.inner());
      final Subquery<Object> subQuery = linkSubQueries(hops);
      final Map<String, From<?, ?>> joinTables = createJoinTables(tupleQuery, selectionPath, orderByAttributes,
          subQuery);
      tupleQuery.where(createWhere(subQuery, lastInfo));
      tupleQuery.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), groups));
      tupleQuery.orderBy(createOrderBy(joinTables, orderByAttributes));
      tupleQuery.distinct(orderByAttributes.isEmpty());
      if (!orderByAttributes.isEmpty()) {
        cq.groupBy(createGroupBy(joinTables, target, selectionPath.joinedPersistent(), orderByAttributes));
      }
      final TypedQuery<Tuple> query = em.createQuery(tupleQuery);
      return new JPAQueryCreationResult(query, selectionPath);
    }
  }

  @Override
  protected SelectionPathInfo<JPAPath> buildSelectionPathList(final UriInfoResource uriResource)
      throws ODataApplicationException {
    try {
      final SelectionPathInfo<JPAPath> jpaPathList = super.buildSelectionPathList(uriResource);
      debugger.trace(this, "The following selection path elements were found: %s", jpaPathList.toString());
      return new SelectionPathInfo<>(association.getRightColumnsList(), jpaPathList);
    } catch (final ODataJPAModelException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), ODataJPAException.getLocales().nextElement(), e);
    }
  }

  Map<String, From<?, ?>> createJoinTables(final ProcessorCriteriaQuery<Tuple> tupleQuery,
      final SelectionPathInfo<JPAPath> selectionPath, final List<JPAProcessorAttribute> orderByAttributes,
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

  @Override
  Expression<Boolean> createWhere(final Subquery<?> subQuery, final JPANavigationPropertyInfo navigationInfo)
      throws ODataApplicationException {

    try (JPARuntimeMeasurement measurement = debugger.newMeasurement(this, "createWhere")) {
      if (hasRowLimit(lastInfo)) {
        return createWhereByRowNumber(target, lastInfo);
      }
      return super.createWhere(subQuery, navigationInfo);
    }
  }
}
