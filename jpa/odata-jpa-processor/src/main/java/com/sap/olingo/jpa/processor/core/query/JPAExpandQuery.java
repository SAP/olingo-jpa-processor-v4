package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
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
 * <li>expandOption = expandRefOption/ <b>select</b>/ <b>expand</b> / levels <p>
 * </ul>
 * As of now only the bold once are supported
 * <p>
 * @author Oliver Grande
 *
 */
public class JPAExpandQuery extends JPAExecutableQuery {
  private final JPAAssociationPath assoziation;
  private final JPAExpandItemInfo item;

  public JPAExpandQuery(final OData odata, final JPAODataSessionContextAccess context, final EntityManager em,
      final UriInfoResource uriInfo, final JPAAssociationPath assoziation, final JPAEntityType entityType,
      final Map<String, List<String>> requestHeaders) throws ODataException {

    super(odata, context, entityType, em, requestHeaders, uriInfo);
    this.assoziation = assoziation;
    this.item = null;
  }

  public JPAExpandQuery(final OData odata, final JPAODataSessionContextAccess context, final EntityManager em,
      final JPAExpandItemInfo item, final Map<String, List<String>> requestHeaders) throws ODataException {

    super(odata, context, item.getEntityType(), em, requestHeaders, item.getUriInfo());
    this.assoziation = item.getExpandAssociation();
    this.item = item;
  }

  public JPAExpandQueryResult execute() throws ODataApplicationException {
    if (uriResource.getTopOption() != null || uriResource.getSkipOption() != null)
      return executeExpandTopSkipQuery();
    else {
      return executeStandardQuery();
    }
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
    // TODO make this replacable e.g. by UNION ALL
    final int handle = debugger.startRuntimeMeasurement("JPAExpandQuery", "executeExpandTopSkipQuery");

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
    final int handle = debugger.startRuntimeMeasurement("JPAExpandQuery", "executeStandradQuery");

    final TypedQuery<Tuple> tupleQuery = createTupleQuery();

    final int resultHandle = debugger.startRuntimeMeasurement("TypedQuery", "getResultList");
    final List<Tuple> intermediateResult = tupleQuery.getResultList();
    debugger.stopRuntimeMeasurement(resultHandle);
    Map<String, List<Tuple>> result = convertResult(intermediateResult, assoziation, 0, Long.MAX_VALUE);

    debugger.stopRuntimeMeasurement(handle);
    return new JPAExpandQueryResult(result, count(), jpaEntity);
  }

  private TypedQuery<Tuple> createTupleQuery() throws ODataApplicationException {
    final int handle = debugger.startRuntimeMeasurement("JPAExpandQuery", "createTupleQuery");

    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final Map<String, From<?, ?>> joinTables = createFromClause(new ArrayList<JPAAssociationAttribute>(),
        descriptionAttributes);

    cq.multiselect(createSelectClause(joinTables, selectionPath));
    cq.where(createWhere(joinTables));

    final List<Order> orderBy = createOrderByJoinCondition(assoziation);
    orderBy.addAll(createOrderByList(joinTables, uriResource.getOrderByOption()));
    cq.orderBy(orderBy);
    // TODO group by also at $expand
    final TypedQuery<Tuple> query = em.createQuery(cq);

    debugger.stopRuntimeMeasurement(handle);
    return query;
  }

  private Long count() {
    // TODO Count and Expand -> Olingo
    return null;
  }

  Map<String, List<Tuple>> convertResult(final List<Tuple> intermediateResult, final JPAAssociationPath a,
      final long skip, final long top) throws ODataApplicationException {
    String joinKey = "";
    long skiped = 0;
    long taken = 0;

    List<Tuple> subResult = null;
    final Map<String, List<Tuple>> convertedResult = new HashMap<String, List<Tuple>>();
    for (final Tuple row : intermediateResult) {
      String actuallKey;
      try {
        actuallKey = buildConcatenatedKey(row, a.getJoinColumnsList());
      } catch (ODataJPAModelException e) {
        throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
      }

      if (!actuallKey.equals(joinKey)) {
        subResult = new ArrayList<Tuple>();
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

  private String buildConcatenatedKey(final Tuple row, final List<JPAOnConditionItem> joinColumns) {
    final StringBuffer buffer = new StringBuffer();
    for (final JPAOnConditionItem item : joinColumns) {
      buffer.append(JPAPath.PATH_SEPERATOR);
      buffer.append(row.get(item.getRightPath().getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  private List<Order> createOrderByJoinCondition(final JPAAssociationPath a) throws ODataApplicationException {
    final List<Order> orders = new ArrayList<Order>();

    try {
      for (final JPAOnConditionItem j : a.getJoinColumnsList()) {
        orders.add(cb.asc(root.get(j.getRightPath().getLeaf().getInternalName())));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.BAD_REQUEST);
    }
    return orders;
  }

  @Override
  protected Expression<Boolean> createWhere(final Map<String, From<?, ?>> joinTables) throws ODataApplicationException {

    Expression<Boolean> whereCondition = null;
    try {
      whereCondition = filter.compile();
    } catch (ExpressionVisitException e) {
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_FILTER_ERROR,
          HttpStatusCode.BAD_REQUEST, e);
    }

    if (whereCondition == null)
      whereCondition = cb.exists(buildSubQueries());// parentQuery.asSubQuery(this, assoziation));
    else
      whereCondition = cb.and(whereCondition, cb.exists(buildSubQueries()));

    return whereCondition;
  }

  private Subquery<?> buildSubQueries() throws ODataApplicationException {
    Subquery<?> childQuery = null;

    final List<UriResource> resourceParts = uriResource.getUriResourceParts();

    // 1. Determine all relevant associations
    final List<JPANavigationProptertyInfo> expandPathList = Util.determineAssoziations(sd, resourceParts);
    expandPathList.addAll(item.getHops());

    // 2. Create the queries and roots
    JPAAbstractQuery parent = this;
    final List<JPANavigationQuery> queryList = new ArrayList<JPANavigationQuery>();

    for (final JPANavigationProptertyInfo naviInfo : expandPathList) {
      queryList.add(new JPANavigationInheritFilterQuery(odata, sd, parent, em, naviInfo));
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQueryExists(childQuery);
    }
    return childQuery;
  }
}
