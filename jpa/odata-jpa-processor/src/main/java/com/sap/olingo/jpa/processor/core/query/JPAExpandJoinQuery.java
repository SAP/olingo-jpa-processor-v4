package com.sap.olingo.jpa.processor.core.query;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceCount;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

/**
 * A query to retrieve the expand entities.<p> According to
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
 * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a> the following query options are allowed:
 * <ul>
 * <li>expandCountOption = <b>filter</b>/ search<p>
 * <li>expandRefOption = expandCountOption/ <b>orderby</b> / <b>skip</b> / <b>top</b> / inlinecount
 * <li>expandOption = expandRefOption/ <b>select</b>/ <b>expand</b> / <b>levels</b> <p>
 * </ul>
 * As of now only the bold once are supported
 * <p>
 * @author Oliver Grande
 *
 */
public final class JPAExpandJoinQuery extends JPAAbstractJoinQuery {
  private final JPAAssociationPath assoziation;
  private final Optional<JPAKeyBoundary> keyBoundary;
  private TypedQuery<Tuple> tupleQuery;

  public JPAExpandJoinQuery(final OData odata, final JPAODataCRUDContextAccess sessionContext,
      final JPAInlineItemInfo item, final Map<String, List<String>> requestHeaders,
      final JPAODataRequestContextAccess requestContext, final Optional<JPAKeyBoundary> keyBoundary)
      throws ODataException {

    super(odata, sessionContext, item.getEntityType(), item.getUriInfo(), requestContext, requestHeaders,
        item.getHops());
    this.assoziation = item.getExpandAssociation();
    this.keyBoundary = keyBoundary;
  }

  public JPAExpandJoinQuery(final OData odata, final JPAODataCRUDContextAccess context,
      final JPAAssociationPath assoziation, final JPAEntityType entityType,
      final Map<String, List<String>> requestHeaders, final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    super(odata, context, entityType, requestContext, requestHeaders, Collections.emptyList());
    this.assoziation = assoziation;
    this.keyBoundary = Optional.empty();
  }

  /**
   * Process a expand query, which may contains a $skip and/or a $top option.<p>
   * This is a tricky problem, as it can not be done easily with SQL. It could be that a database offers special
   * solutions. There is an worth reading blog regards this topic:
   * <a href="http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/">How to select
   * the first/least/max row per group in SQL</a>. Often databases offer the option to use <code>ROW_NUMBER</code>
   * together with <code>OVER ... ORDER BY</code> see e.g. <a
   * href="http://www.sqltutorial.org/sql-window-functions/sql-row_number/">SQL ROW_NUMBER</a>.
   * Unfortunately this is not supported by JPA.
   * @return query result
   * @throws ODataApplicationException
   */
  @Override
  public JPAExpandQueryResult execute() throws ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement(this, "execute");

    long skip = 0;
    long top = Long.MAX_VALUE;
    try {
      tupleQuery = createTupleQuery();

      final int resultHandle = debugger.startRuntimeMeasurement(tupleQuery, "getResultList");
      final List<Tuple> intermediateResult = tupleQuery.getResultList();
      debugger.stopRuntimeMeasurement(resultHandle);
      if (uriResource.getTopOption() != null || uriResource.getSkipOption() != null) {
        // Simplest solution for the problem. Read all and throw away, what is not requested
        if (uriResource.getSkipOption() != null)
          skip = uriResource.getSkipOption().getValue();
        if (uriResource.getTopOption() != null)
          top = uriResource.getTopOption().getValue();
      }
      final Map<String, List<Tuple>> result = convertResult(intermediateResult, assoziation, skip, top);

      final Set<JPAPath> requestedSelection = new HashSet<>();
      buildSelectionAddNavigationAndSelect(uriResource, requestedSelection, uriResource.getSelectOption());
      debugger.stopRuntimeMeasurement(handle);
      return new JPAExpandQueryResult(result, count(), jpaEntity, requestedSelection);

    } catch (final JPANoSelectionException e) {
      return new JPAExpandQueryResult(Collections.emptyMap(), Collections.emptyMap(), this.jpaEntity, Collections
          .emptyList());
    } catch (final ODataJPAModelException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), ODataJPAModelException.getLocales().nextElement(), e);
    }
  }

  /**
   * Returns the generated SQL string after the query has been executed, otherwise an empty string.<br>
   * As of now this is only supported for EcliseLink
   * @return
   * @throws ODataJPAQueryException
   */
  String getSQLString() throws ODataJPAQueryException {
    if (tupleQuery != null && tupleQuery.getClass().getCanonicalName().equals(
        "org.eclipse.persistence.internal.jpa.EJBQueryImpl")) {

      try {
        final Object dbQuery = tupleQuery.getClass().getMethod("getDatabaseQuery").invoke(tupleQuery);
        return (String) dbQuery.getClass().getMethod("getSQLString").invoke(dbQuery);
      } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
          | InvocationTargetException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } else {
      return "";
    }
  }

  @Override
  protected List<Selection<?>> createSelectClause(final Map<String, From<?, ?>> joinTables,
      final Collection<JPAPath> jpaPathList,
      final From<?, ?> target, final List<String> groups) throws ODataApplicationException {

    final List<Selection<?>> selections = new ArrayList<>(super.createSelectClause(joinTables, jpaPathList, target,
        groups));
    if (assoziation.getJoinTable() != null) {
      // For associations with JoinTable the join columns, linking columns to the parent, need to be added
      createAdditionSelctionForJoinTable(selections);
    }
    return selections;
  }

  private void createAdditionSelctionForJoinTable(final List<Selection<?>> selections) throws ODataJPAQueryException {
    final From<?, ?> parent = determineParentFrom(); // e.g. JoinSource
    try {
      for (final JPAPath p : assoziation.getLeftColumnsList()) {
        final Path<?> selection = ExpressionUtil.convertToCriteriaPath(parent, p.getPath());
        // If source and target of an association use the same name for their key we get conflicts with the alias.
        // Therefore it is necessary to unify them.
        selection.alias(assoziation.getAlias() + ALIAS_SEPERATOR + p.getAlias());
        selections.add(selection);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Splits up a expand results, so it is returned as a map that uses a concatenation of the field values know by the
   * parent.
   * @param intermediateResult
   * @param associationPath
   * @param skip
   * @param top
   * @return
   * @throws ODataApplicationException
   */
  Map<String, List<Tuple>> convertResult(final List<Tuple> intermediateResult, final JPAAssociationPath associationPath,
      final long skip, final long top) throws ODataApplicationException {
    String joinKey = "";
    long skiped = 0;
    long taken = 0;

    List<Tuple> subResult = null;
    final Map<String, List<Tuple>> convertedResult = new HashMap<>();
    for (final Tuple row : intermediateResult) {
      String actuallKey;
      try {
        actuallKey = buildConcatenatedKey(row, associationPath);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
      }

      if (!actuallKey.equals(joinKey)) {
        subResult = new ArrayList<>();
        convertedResult.put(actuallKey, subResult);
        joinKey = actuallKey;
        skiped = taken = 0;
      }
      if (skiped >= skip && taken < top) {
        taken += 1;
        subResult.add(row);
      } else {
        skiped += 1;
      }
    }
    return convertedResult;
  }

  private String buildConcatenatedKey(final Tuple row, final JPAAssociationPath associationPath)
      throws ODataJPAModelException {

    if (associationPath.getJoinTable() == null) {
      final List<JPAPath> joinColumns = associationPath.getRightColumnsList();
      return joinColumns.stream()
          .map(c -> (row.get(c
              .getAlias()))
                  .toString())
          .collect(joining(JPAPath.PATH_SEPERATOR));
    } else {
      final List<JPAPath> joinColumns = associationPath.getLeftColumnsList();
      return joinColumns.stream()
          .map(c -> (row.get(assoziation.getAlias() + ALIAS_SEPERATOR + c.getAlias())).toString())
          .collect(joining(JPAPath.PATH_SEPERATOR));
    }
  }

  private List<Expression<?>> buildExpandCountGroupBy() throws ODataJPAQueryException {

    final List<Expression<?>> groupBy = new ArrayList<>();
    try {
      final List<JPAOnConditionItem> associationPathList = assoziation.getJoinColumnsList();
      for (final JPAOnConditionItem onCondition : associationPathList) {
        groupBy.add(ExpressionUtil.convertToCriteriaPath(target, onCondition.getRightPath().getPath()));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return groupBy;
  }

  private List<Selection<?>> buildExpandJoinPath() throws ODataApplicationException {
    final List<Selection<?>> selections = new ArrayList<>();
    try {
      final List<JPAOnConditionItem> associationPathList = assoziation.getJoinColumnsList();
      for (final JPAOnConditionItem onCondition : associationPathList) {
        final Path<?> p = ExpressionUtil.convertToCriteriaPath(target, onCondition.getRightPath().getPath());
        p.alias(onCondition.getRightPath().getAlias());
        selections.add(p);
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return selections;
  }

  private Map<String, Long> convertCountResult(final List<Tuple> intermediateResult) throws ODataJPAQueryException {
    final Map<String, Long> result = new HashMap<>();
    for (final Tuple row : intermediateResult) {
      try {
        final String actuallKey = buildConcatenatedKey(row, assoziation);
        final Long count = (Long) row.get("$count");
        result.put(actuallKey, count);
      } catch (final ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
      }
    }
    return result;
  }

  private Map<String, Long> count() throws ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement(this, "count");
    final List<UriResource> uriResourceParts = uriResource.getUriResourceParts();
    if (uriResource.getCountOption() != null
        || uriResourceParts != null
            && !uriResourceParts.isEmpty()
            && uriResourceParts.get(uriResourceParts.size() - 1) instanceof UriResourceCount) {
//      SELECT "BusinessPartnerID", count(*)
//      FROM findings_test."sap.hc.studyproxy::Config.ReferenceRanges"
//        WHERE "BusinessPartnerID"
//        GROUP BY "BusinessPartnerID"
      final CriteriaQuery<Tuple> countQuery = cb.createTupleQuery();
      final List<Selection<?>> selectionPath = buildExpandJoinPath();

      final Expression<Long> count = cb.count(target);
      count.alias("$count");
      selectionPath.add(count);
      countQuery.multiselect(selectionPath);
      final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
      if (whereClause != null)
        cq.where(whereClause);
      countQuery.groupBy(buildExpandCountGroupBy());
      final TypedQuery<Tuple> query = em.createQuery(countQuery);
      final List<Tuple> intermediateResult = query.getResultList();
      return convertCountResult(intermediateResult);
    }
    debugger.stopRuntimeMeasurement(handle);
    return null;
  }

  private List<Order> createOrderByJoinCondition(final JPAAssociationPath associationPath)
      throws ODataApplicationException {
    final List<Order> orders = new ArrayList<>();

    try {
      final List<JPAPath> joinColumns = associationPath.getJoinTable() == null
          ? associationPath.getRightColumnsList() : associationPath.getLeftColumnsList();
      final From<?, ?> from = associationPath.getJoinTable() == null
          ? target : determineParentFrom();

      for (final JPAPath j : joinColumns) {
        Path<?> jpaProperty = from;
        for (final JPAElement pathElement : j.getPath()) {
          jpaProperty = jpaProperty.get(pathElement.getInternalName());
        }
        orders.add(cb.asc(jpaProperty));
      }
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return orders;
  }

  private TypedQuery<Tuple> createTupleQuery() throws ODataApplicationException, JPANoSelectionException {
    final int handle = debugger.startRuntimeMeasurement(this, "createTupleQuery");

    final Set<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final Map<String, From<?, ?>> joinTables = createFromClause(new ArrayList<JPAAssociationPath>(1),
        selectionPath, cq, lastInfo);

    // TODO handle Join Column is ignored
    cq.multiselect(createSelectClause(joinTables, selectionPath, target, groups));
    cq.distinct(true);
    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
    if (whereClause != null)
      cq.where(whereClause);

    final List<Order> orderBy = createOrderByJoinCondition(assoziation);
    orderBy.addAll(new JPAOrderByBuilder(jpaEntity, target, cb, groups).createOrderByList(joinTables, uriResource));
    cq.orderBy(orderBy);
    // TODO group by also at $expand
    final TypedQuery<Tuple> query = em.createQuery(cq);

    debugger.stopRuntimeMeasurement(handle);
    return query;
  }

  private Expression<Boolean> createWhere() throws ODataApplicationException {

    final int handle = debugger.startRuntimeMeasurement(this, "createWhere");

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    // Given keys: Organizations('1')/Roles(...)
    try {
      whereCondition = createKeyWhere(navigationInfo);
      whereCondition = addWhereClause(whereCondition, createBoundary(navigationInfo, keyBoundary));
      whereCondition = addWhereClause(whereCondition, createExpandWhere());
      whereCondition = addWhereClause(whereCondition, createProtectionWhere(claimsProvider));
    } catch (final ODataApplicationException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    }
    debugger.stopRuntimeMeasurement(handle);
    return whereCondition;
  }

  private javax.persistence.criteria.Expression<Boolean> createExpandWhere() throws ODataApplicationException {

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    for (final JPANavigationProptertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        try {
          whereCondition = addWhereClause(whereCondition, info.getFilterCompiler().compile());
        } catch (final ExpressionVisitException e) {
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
              HttpStatusCode.BAD_REQUEST, e);
        }
      }
    }
    return whereCondition;
  }

  private From<?, ?> determineParentFrom() throws ODataJPAQueryException {
    for (final JPANavigationProptertyInfo item : this.navigationInfo) {
      if (item.getAssociationPath() == assoziation)
        return item.getFromClause();
    }
    throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
        HttpStatusCode.BAD_REQUEST);
  }

  @Override
  protected Set<JPAPath> buildSelectionPathList(final UriInfoResource uriResource) throws ODataApplicationException {
    try {
      final Set<JPAPath> jpaPathList = super.buildSelectionPathList(uriResource);
      jpaPathList.addAll(assoziation.getRightColumnsList());
      return jpaPathList;
    } catch (final ODataJPAModelException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR
          .getStatusCode(), ODataJPAModelException.getLocales().nextElement(), e);
    }
  }

}
