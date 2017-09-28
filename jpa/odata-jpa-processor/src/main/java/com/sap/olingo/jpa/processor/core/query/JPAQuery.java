package com.sap.olingo.jpa.processor.core.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPAQuery extends JPAExecutableQuery {

  public JPAQuery(final OData odata, final EdmEntitySet entitySet, final JPAODataSessionContextAccess context,
      final UriInfo uriInfo, final EntityManager em, final Map<String, List<String>> requestHeaders)
      throws ODataException {

    super(odata, context, context.getEdmProvider().getServiceDocument().getEntity(entitySet.getName()), em,
        requestHeaders, uriInfo);
  }

  /**
   * Counts the number of results to be expected by a query. The method shall fulfill the requirements of the $count
   * query option. This is defined as follows:<p>
   * <i>The $count system query option ignores any $top, $skip, or $expand query options, and returns the total count
   * of results across all pages including only those results matching any specified $filter and $search.</i><p>
   * For details see: <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398308"
   * >OData Version 4.0 Part 1 - 11.2.5.5 System Query Option $count</a>
   * 
   * @return Number of results
   * @throws ODataApplicationException
   * @throws ExpressionVisitException
   */
  public Long countResults() throws ODataApplicationException {
    /*
     * URL example:
     * .../Organizations?$count=true
     * .../Organizations/count
     * .../Organizations('3')/Roles/$count
     */
    final int handle = debugger.startRuntimeMeasurement(this, "countResults");
    final HashMap<String, From<?, ?>> joinTables = new HashMap<>();

    final CriteriaQuery<Long> cq = cb.createQuery(Long.class);

    joinTables.put(jpaEntity.getTypeClass().getCanonicalName(), root);

    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere(joinTables);
    if (whereClause != null)
      cq.where(whereClause);
    cq.select(cb.count(root));
    debugger.stopRuntimeMeasurement(handle);
    return em.createQuery(cq).getSingleResult();
  }

  public JPAExpandQueryResult execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    // TODO check if Path is also required for OrderBy Attributes, as it is for descriptions
    final int handle = debugger.startRuntimeMeasurement(this, "execute");

    final List<JPAAssociationAttribute> orderByNaviAttributes = extractOrderByNaviAttributes();
    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final Map<String, From<?, ?>> joinTables = createFromClause(orderByNaviAttributes, descriptionAttributes);

    cq.multiselect(createSelectClause(joinTables, selectionPath));

    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere(joinTables);
    if (whereClause != null)
      cq.where(whereClause);

    cq.orderBy(createOrderByList(joinTables, uriResource.getOrderByOption()));

    if (!orderByNaviAttributes.isEmpty())
      cq.groupBy(createGroupBy(joinTables, selectionPath));

    final TypedQuery<Tuple> tq = em.createQuery(cq);
    addTopSkip(tq);

    final HashMap<String, List<Tuple>> result = new HashMap<>(1);
    final int resultHandle = debugger.startRuntimeMeasurement(tq, "getResultList");
    final List<Tuple> intermediateResult = tq.getResultList();
    debugger.stopRuntimeMeasurement(resultHandle);
    result.put("root", intermediateResult);

    debugger.stopRuntimeMeasurement(handle);
    return new JPAExpandQueryResult(result, Long.parseLong("0"), jpaEntity);
  }

  public JPAStructuredType getEntityType() {
    return jpaEntity;
  }

  public SelectOption getSelectOption() {
    return uriResource.getSelectOption();
  }

  private List<javax.persistence.criteria.Expression<?>> createGroupBy(final Map<String, From<?, ?>> joinTables,
      final List<JPAPath> selectionPathList) {
    final int handle = debugger.startRuntimeMeasurement(this, "createGroupBy");

    final List<javax.persistence.criteria.Expression<?>> groupBy =
        new ArrayList<>();

    for (final JPAPath jpaPath : selectionPathList) {
      groupBy.add(ExpressionUtil.convertToCriteriaPath(joinTables, root, jpaPath));
    }

    debugger.stopRuntimeMeasurement(handle);
    return groupBy;
  }

  private List<JPAAssociationAttribute> extractOrderByNaviAttributes() throws ODataApplicationException {
    final List<JPAAssociationAttribute> naviAttributes = new ArrayList<>();

    final OrderByOption orderBy = uriResource.getOrderByOption();
    if (orderBy != null) {
      for (final OrderByItem orderByItem : orderBy.getOrders()) {
        final Expression expression = orderByItem.getExpression();
        if (expression instanceof Member) {
          final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
          for (final UriResource uriResource : resourcePath.getUriResourceParts()) {
            if (uriResource instanceof UriResourceNavigation) {
              final EdmNavigationProperty edmNaviProperty = ((UriResourceNavigation) uriResource).getProperty();
              try {
                naviAttributes.add(jpaEntity.getAssociationPath(edmNaviProperty.getName())
                    .getLeaf());
              } catch (ODataJPAModelException e) {
                throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
                    HttpStatusCode.INTERNAL_SERVER_ERROR, e);
              }
            }
          }
        }
      }
    }
    return naviAttributes;
  }

}
