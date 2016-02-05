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
import org.apache.olingo.jpa.processor.core.api.Util;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResourcePartTyped;

/**
 * A query to retrieve the expand entities.<p> According to
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part2-url-conventions/odata-v4.0-errata02-os-part2-url-conventions-complete.html#_Toc406398162"
 * >OData Version 4.0 Part 2 - 5.1.2 System Query Option $expand</a> the following query options have are allowed:
 * <ul>
 * <li>expandCountOption = <b>filter</b>/ search<p>
 * <li>expandRefOption = expandCountOption/ <b>orderby</b> / <b>skip</b> / <b>top</b> / <b>inlinecount</b>
 * <li><b>expandOption</b> = expandRefOption/ <b>select</b>/ <b>expand</b> / levels
 * 
 * As of now only the bold once are planed to be supported
 * 
 * @author Oliver Grande
 *
 */
public class JPAExpandQuery extends JPAExecutableQuery {
  private final JPAExecutableQuery parentQuery;
  private final JPAAssociationPath assoziation;

  public JPAExpandQuery(ServicDocument sd, EntityManager em, UriInfoResource uriInfo, JPAAssociationPath assoziation,
      JPAExecutableQuery parentQuery, Map<String, List<String>> requestHeaders)
          throws ODataApplicationException {
    super(sd, Util.determineTargetEntityType(uriInfo.getUriResourceParts()), em, requestHeaders,
        uriInfo);
    this.parentQuery = parentQuery;
    this.assoziation = assoziation;
  }

  public JPAExpandResult execute() throws ODataApplicationException {
    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final HashMap<String, From<?, ?>> joinTables = createFromClause(new ArrayList<JPAAssociationAttribute>(),
        descriptionAttributes);

    cq.multiselect(createSelectClause(joinTables, selectionPath));
    cq.where(createWhere(joinTables));

    List<Order> orderBy = new ArrayList<Order>();// createOrderByJoinCondition(assoziation);
    orderBy.addAll(createOrderList(joinTables, uriResource.getOrderByOption()));
    cq.orderBy(orderBy);

    TypedQuery<Tuple> tq = em.createQuery(cq);

    List<Tuple> intermediateResult = tq.getResultList();
    return new JPAExpandResult(convertResult(intermediateResult, assoziation));
  }

  Map<String, List<Tuple>> convertResult(List<Tuple> intermediateResult, JPAAssociationPath a)
      throws ODataApplicationException {
    String joinKey = "";

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
      }
      subResult.add(row);
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

    javax.persistence.criteria.Expression<Boolean> whereCondition = super.createWhere(joinTables);

    if (whereCondition == null)
      whereCondition = cb.exists(buildSubQueries());// parentQuery.asSubQuery(this, assoziation));
    else
      whereCondition = cb.and(whereCondition, cb.exists(parentQuery.asSubQuery(this, assoziation)));

    return whereCondition;
  }

  @Override
  protected <T extends Object> Subquery<T> asSubQuery(JPAAbstractQuery parent, JPAAssociationPath assoziation)
      throws ODataApplicationException {

    JPANavigationQuery subQuery = new JPANavigationQuery(sd, (UriResourcePartTyped) uriResource.getUriResourceParts()
        .get(0), parent, em, assoziation);

    return subQuery.getSubQueryExists(null); // parentQuery.asSubQuery(this, this.assoziation));
  }

  @Override
  protected List<JPANavigationQuery> asSubQueries(JPAAbstractQuery superordinateQuery,
      JPAAssociationPath assoziation) throws ODataApplicationException {
    List<JPANavigationQuery> subQueries = parentQuery.asSubQueries(this, this.assoziation);

    JPANavigationQuery subQuery = new JPANavigationQuery(sd, (UriResourcePartTyped) uriResource.getUriResourceParts()
        .get(0), superordinateQuery, em, assoziation);

    subQueries.add(subQuery);
    return subQueries;
  }

  private Subquery<?> buildSubQueries() throws ODataApplicationException {
    List<JPANavigationQuery> subQueries = parentQuery.asSubQueries(this, this.assoziation);

    Subquery<?> childQuery = null;
    for (int i = 0; i < subQueries.size(); i++) {
      childQuery = subQueries.get(i).getSubQueryExists(childQuery);
    }
    return childQuery;
  }
}
