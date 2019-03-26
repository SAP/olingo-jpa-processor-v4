package com.sap.olingo.jpa.processor.core.query;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
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
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
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

  public JPAExpandJoinQuery(final OData odata, final JPAODataSessionContextAccess context, final EntityManager em,
      final JPAInlineItemInfo item, final Map<String, List<String>> requestHeaders) throws ODataException {

    super(odata, context, item.getEntityType(), em, requestHeaders, item.getUriInfo(), null);
    this.assoziation = item.getExpandAssociation();
    this.navigationInfo = item.getHops();
  }

  /**
   * Use for testing
   * @param odata
   * @param context
   * @param em
   * @param uriInfo
   * @param assoziation
   * @param entityType
   * @param requestHeaders
   * @throws ODataException
   */
  public JPAExpandJoinQuery(final OData odata, final JPAODataSessionContextAccess context, final EntityManager em,
      final UriInfoResource uriInfo, final JPAAssociationPath assoziation, final JPAEntityType entityType,
      final Map<String, List<String>> requestHeaders) throws ODataException {

    super(odata, context, entityType, em, requestHeaders, uriInfo, null);
    this.assoziation = assoziation;
  }

  public JPAExpandQueryResult execute() throws ODataApplicationException {
    if (uriResource.getTopOption() != null || uriResource.getSkipOption() != null)
      return executeExpandTopSkipQuery();
    else {
      return executeStandardQuery();
    }
  }

  @Override
  protected List<Selection<?>> createSelectClause(Map<String, From<?, ?>> joinTables, List<JPAPath> jpaPathList,
      From<?, ?> target) throws ODataApplicationException {

    final List<Selection<?>> selections = new ArrayList<>(super.createSelectClause(joinTables, jpaPathList, target));
    if (assoziation.getJoinTable() != null) {
      // For associations with JoinTable the join columns, linking columns to the parent, need to be added
      createAdditionSelctionForJoinTable(selections);
    }
    return selections;
  }

  private void createAdditionSelctionForJoinTable(final List<Selection<?>> selections) throws ODataJPAQueryException {
    final From<?, ?> parent = determineParentFrom(); // e.g. JoinSource
    try {
      for (JPAPath p : assoziation.getLeftColumnsList()) {
        final Path<?> selection = ExpressionUtil.convertToCriteriaPath(parent, p.getPath());
        // If source and target of an association use the same name for their key we get conflicts with the alias.
        // Therefore it is necessary to unify them.
        selection.alias(assoziation.getAlias() + ALIAS_SEPERATOR + p.getAlias());
        selections.add(selection);
      }
    } catch (ODataJPAModelException e) {
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
      } catch (ODataJPAModelException e) {
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
      } else
        skiped += 1;
    }
    return convertedResult;
  }

  private String buildConcatenatedKey(final Tuple row, final JPAAssociationPath associationPath)
      throws ODataJPAModelException {

    if (associationPath.getJoinTable() == null) {
      final List<JPAPath> joinColumns = associationPath.getRightColumnsList();
      return joinColumns.stream()
          .map(c -> (row.get(c.getAlias())).toString())
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
      for (JPAOnConditionItem onCondition : associationPathList) {
        groupBy.add(ExpressionUtil.convertToCriteriaPath(target, onCondition.getRightPath().getPath()));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return groupBy;
  }

  private List<Selection<?>> buildExpandJoinPath() throws ODataApplicationException {
    final List<Selection<?>> selections = new ArrayList<>();
    try {
      final List<JPAOnConditionItem> associationPathList = assoziation.getJoinColumnsList();
      for (JPAOnConditionItem onCondition : associationPathList) {
        final Path<?> p = ExpressionUtil.convertToCriteriaPath(target, onCondition.getRightPath().getPath());
        p.alias(onCondition.getRightPath().getAlias());
        selections.add(p);
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return selections;
  }

  private Map<String, Long> convertCountResult(List<Tuple> intermediateResult) throws ODataJPAQueryException {
    final Map<String, Long> result = new HashMap<>();
    for (Tuple row : intermediateResult) {
      try {
        final String actuallKey = buildConcatenatedKey(row, assoziation);
        final Long count = (Long) row.get("$count");
        result.put(actuallKey, count);
      } catch (ODataJPAModelException e) {
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

      Expression<Long> count = cb.count(target);
      count.alias("$count");
      selectionPath.add(count);
      countQuery.multiselect(selectionPath);
      final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
      if (whereClause != null)
        cq.where(whereClause);
      countQuery.groupBy(buildExpandCountGroupBy());
      final TypedQuery<Tuple> query = em.createQuery(countQuery);
      List<Tuple> intermediateResult = query.getResultList();
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
        for (JPAElement pathElement : j.getPath()) {
          jpaProperty = jpaProperty.get(pathElement.getInternalName());
        }
        orders.add(cb.asc(jpaProperty));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return orders;
  }

  private TypedQuery<Tuple> createTupleQuery() throws ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement(this, "createTupleQuery");

    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);

    final Map<String, From<?, ?>> joinTables = createFromClause(new ArrayList<JPAAssociationPath>(1),
        descriptionAttributes, cq);

    // TODO handle Join Column is ignored
    cq.multiselect(createSelectClause(joinTables, selectionPath, target));
    cq.distinct(true);
    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
    if (whereClause != null)
      cq.where(whereClause);

    final List<Order> orderBy = createOrderByJoinCondition(assoziation);
    orderBy.addAll(createOrderByList(joinTables, uriResource.getOrderByOption()));
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
    } catch (ODataApplicationException e) {
      debugger.stopRuntimeMeasurement(handle);
      throw e;
    }

    for (JPANavigationProptertyInfo info : this.navigationInfo) {
      if (info.getFilterCompiler() != null) {
        try {
          whereCondition = addWhereClause(whereCondition, info.getFilterCompiler().compile());
        } catch (ExpressionVisitException e) {
          debugger.stopRuntimeMeasurement(handle);
          throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
              HttpStatusCode.BAD_REQUEST, e);
        }
      }
    }
    debugger.stopRuntimeMeasurement(handle);
    return whereCondition;
  }

  private From<?, ?> determineParentFrom() throws ODataJPAQueryException {
    for (JPANavigationProptertyInfo item : this.navigationInfo) {
      if (item.getAssociationPath() == assoziation)
        return item.getFromClause();
    }
    throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
        HttpStatusCode.BAD_REQUEST);
  }

  /**
   * Process a expand query, which contains a $skip and/or a $top option.<p>
   * This is a tricky problem, as it can not be done easily with SQL. It could be that a database offers special
   * solutions.
   * There is an worth reading blog regards this topic:
   * <a href="http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/">How to select
   * the first/least/max row per group in SQL</a>
   * @return query result
   * @throws ODataApplicationException
   */
  private JPAExpandQueryResult executeExpandTopSkipQuery() throws ODataApplicationException {
    // TODO make this replacable e.g. by UNION ALL or use of window functions
    final int handle = debugger.startRuntimeMeasurement(this, "executeExpandTopSkipQuery");

    long skip = 0;
    long top = Long.MAX_VALUE;
    final TypedQuery<Tuple> tupleQuery = createTupleQuery();
    // Simplest solution for the problem. Read all and throw away, what is not requested
    final List<Tuple> intermediateResult = tupleQuery.getResultList();
    if (uriResource.getSkipOption() != null)
      skip = uriResource.getSkipOption().getValue();
    if (uriResource.getTopOption() != null)
      top = uriResource.getTopOption().getValue();

    Map<String, List<Tuple>> result = convertResult(intermediateResult, assoziation, skip, top);
    debugger.stopRuntimeMeasurement(handle);
    return new JPAExpandQueryResult(result, count(), jpaEntity);
  }

  private JPAExpandQueryResult executeStandardQuery() throws ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement(this, "executeStandradQuery");

    final TypedQuery<Tuple> tupleQuery = createTupleQuery();
    final int resultHandle = debugger.startRuntimeMeasurement(tupleQuery, "getResultList");
    final List<Tuple> intermediateResult = tupleQuery.getResultList();
    debugger.stopRuntimeMeasurement(resultHandle);

    Map<String, List<Tuple>> result = convertResult(intermediateResult, assoziation, 0, Long.MAX_VALUE);

    debugger.stopRuntimeMeasurement(handle);
    return new JPAExpandQueryResult(result, count(), jpaEntity);
  }
}
