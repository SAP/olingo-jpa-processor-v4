package org.apache.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPAAssociationPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;

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
 * 
 * @author Oliver Grande
 *
 */
public class JPAExpandQuery extends JPAExecutableQuery {
  private final JPAAssociationPath assoziation;
  private final JPAExpandItemInfo item;

  public JPAExpandQuery(OData odata, ServicDocument sd, EntityManager em, UriInfoResource uriInfo,
      JPAAssociationPath assoziation,
      JPAExecutableQuery parentQuery, Map<String, List<String>> requestHeaders)
          throws ODataApplicationException {
    super(odata, sd, Util.determineTargetEntityType(uriInfo.getUriResourceParts()), em, requestHeaders,
        uriInfo);
    this.assoziation = assoziation;
    this.item = null;
  }

  public JPAExpandQuery(OData odata, ServicDocument sd, EntityManager em, JPAExpandItemInfo item,
      Map<String, List<String>> requestHeaders) throws ODataApplicationException {

    super(odata, sd, Util.determineTargetEntityType(item.getUriInfo().getUriResourceParts()), em, requestHeaders,
        item.getUriInfo());
    this.assoziation = item.getExpandAssociation();
    this.item = item;
  }

  public JPAExpandResult execute() throws ODataApplicationException {
    if (uriResource.getTopOption() != null || uriResource.getSkipOption() != null)
      return executeExpandTopSkipQuery();
    else {
      return executeStandradQuery();
    }
  }

  /**
   * Process a expand query, which contains a $skip and/or a $top option.<p>
   * This a tricky problem, as it can not be done easily with SQL. It could be that a database offers special solutions.
   * There is an worth reading blog regards this topic:
   * <a href="http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/">How to select
   * the first/least/max row per group in SQL</a>
   * @return query result
   * @throws ODataApplicationException
   */
  private JPAExpandResult executeExpandTopSkipQuery() throws ODataApplicationException {
    long skip = 0;
    long top = Long.MAX_VALUE;
    TypedQuery<Tuple> tq = createTupleQuery();
    // Simplest solution for the problem. Read all and throw away, what is not requested
    List<Tuple> intermediateResult = tq.getResultList();
    if (uriResource.getSkipOption() != null) skip = uriResource.getSkipOption().getValue();
    if (uriResource.getTopOption() != null) top = uriResource.getTopOption().getValue();

    return new JPAExpandResult(convertResult(intermediateResult, assoziation, skip, top), count());
  }

  private JPAExpandResult executeStandradQuery() throws ODataApplicationException {
    TypedQuery<Tuple> tq = createTupleQuery();
    List<Tuple> intermediateResult = tq.getResultList();
    return new JPAExpandResult(convertResult(intermediateResult, assoziation, 0, Long.MAX_VALUE), count());
  }

  private TypedQuery<Tuple> createTupleQuery() throws ODataApplicationException {
    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final HashMap<String, From<?, ?>> joinTables = createFromClause(new ArrayList<JPAAssociationAttribute>(),
        descriptionAttributes);

    cq.multiselect(createSelectClause(joinTables, selectionPath));
    cq.where(createWhere(joinTables));

    List<Order> orderBy = createOrderByJoinCondition(assoziation);
    orderBy.addAll(createOrderList(joinTables, uriResource.getOrderByOption()));
    cq.orderBy(orderBy);
    // TODO group by also at $expand
    TypedQuery<Tuple> tq = em.createQuery(cq);
    return tq;
  }

  private Long count() {
    // TODO Auto-generated method stub
    return null;
  }

  Map<String, List<Tuple>> convertResult(final List<Tuple> intermediateResult, final JPAAssociationPath a,
      final long skip, final long top)
          throws ODataApplicationException {
    String joinKey = "";
    long skiped = 0;
    long taken = 0;

    List<Tuple> subResult = null;
    Map<String, List<Tuple>> convertedResult = new HashMap<String, List<Tuple>>();
    for (Tuple row : intermediateResult) {
      String actuallKey;
      try {
        actuallKey = buildConcatenatedKey(row, a.getJoinColumnsList());
      } catch (ODataJPAModelException e) {
        throw new ODataApplicationException("Join condition not found", HttpStatusCode.BAD_REQUEST.ordinal(),
            Locale.ENGLISH, e);
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

  private String buildConcatenatedKey(Tuple row, List<JPAOnConditionItem> joinColumns) {
    StringBuffer buffer = new StringBuffer();
    for (JPAOnConditionItem item : joinColumns) {
      buffer.append(JPAPath.PATH_SEPERATOR);
      buffer.append(row.get(item.getRightPath().getAlias()));
    }
    buffer.deleteCharAt(0);
    return buffer.toString();
  }

  private List<Order> createOrderByJoinCondition(JPAAssociationPath a) throws ODataApplicationException {
    List<Order> orders = new ArrayList<Order>();

    try {
      for (JPAOnConditionItem j : a.getJoinColumnsList()) {
        orders.add(cb.asc(root.get(j.getRightPath().getLeaf().getInternalName())));
      }
    } catch (ODataJPAModelException e) {
      throw new ODataApplicationException("Join condition not found", HttpStatusCode.BAD_REQUEST.ordinal(),
          Locale.ENGLISH, e);
    }
    return orders;
  }

  @Override
  protected Expression<Boolean> createWhere(HashMap<String, From<?, ?>> joinTables) throws ODataApplicationException {

    javax.persistence.criteria.Expression<Boolean> whereCondition = null;
    try {
      whereCondition = filter.compile();
    } catch (ExpressionVisitException e) {
      throw new ODataApplicationException("Unable to parth filter expression", HttpStatusCode.BAD_REQUEST
          .getStatusCode(), Locale.ENGLISH, e);
    }

    if (whereCondition == null)
      whereCondition = cb.exists(buildSubQueries());// parentQuery.asSubQuery(this, assoziation));
    else
      whereCondition = cb.and(whereCondition, cb.exists(buildSubQueries()));

    return whereCondition;
  }

  private Subquery<?> buildSubQueries() throws ODataApplicationException {
    Subquery<?> childQuery = null;

    List<UriResource> resourceParts = uriResource.getUriResourceParts();

    // 1. Determine all relevant associations
    List<JPANavigationProptertyInfo> expandPathList = Util.determineAssoziations(sd, resourceParts);
    expandPathList.addAll(item.getHops());

    // 2. Create the queries and roots
    JPAAbstractQuery parent = this;
    List<JPANavigationQuery> queryList = new ArrayList<JPANavigationQuery>();

    for (JPANavigationProptertyInfo naviInfo : expandPathList) {
      queryList.add(new JPANavigationQuery(sd, naviInfo.getUriResiource(), parent, em, naviInfo.getAssociationPath()));
      parent = queryList.get(queryList.size() - 1);
    }
    // 3. Create select statements
    for (int i = queryList.size() - 1; i >= 0; i--) {
      childQuery = queryList.get(i).getSubQueryExists(childQuery);
    }
    return childQuery;
  }
}
