package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.queryoption.OrderByItem;
import org.apache.olingo.server.api.uri.queryoption.OrderByOption;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;
import org.apache.olingo.server.api.uri.queryoption.expression.Member;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPAJoinQuery extends JPAAbstractJoinQuery implements JPAQuery, JPACountQuery {

  public JPAJoinQuery(final OData odata, final JPAODataSessionContextAccess sessionContext, final EntityManager em,
      final Map<String, List<String>> requestHeaders, final JPAODataPage page) throws ODataException {

    super(odata, sessionContext, sessionContext.getEdmProvider().getServiceDocument().getEntity(
        Util.determineTargetEntitySet(page.getUriInfo().getUriResourceParts()).getName()),
        em, requestHeaders, page.getUriInfo(), page);

    this.navigationInfo = Util.determineNavigationPath(sd, uriResource.getUriResourceParts(), page.getUriInfo());
  }

  public JPAJoinQuery(final OData odata, final JPAODataSessionContextAccess sessionContext, final EntityManager em,
      final Map<String, List<String>> requestHeaders, final UriInfo uriInfo) throws ODataException {

    super(odata, sessionContext, sessionContext.getEdmProvider().getServiceDocument().getEntity(
        Util.determineTargetEntitySet(uriInfo.getUriResourceParts()).getName()),
        em, requestHeaders, uriInfo, null);

    this.navigationInfo = Util.determineNavigationPath(sd, uriResource.getUriResourceParts(), uriInfo);

  }

  /**
   * Fulfill $count requests. For details see
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752288"
   * >OData Version 4.0 Part 1 - 11.2.5.5 System Query Option $count</a>
   * @return
   * @throws ODataApplicationException
   */
  @Override
  public Long countResults() throws ODataApplicationException {
    /*
     * URL example:
     * .../Organizations?$count=true
     * .../Organizations/$count
     * .../Organizations('3')/Roles/$count
     */
    final int handle = debugger.startRuntimeMeasurement(this, "countResults");
    final CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
    createFromClause(new ArrayList<>(1), new ArrayList<>(1), countQuery);

    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
    if (whereClause != null)
      countQuery.where(whereClause);
    countQuery.select(cb.count(root));
    debugger.stopRuntimeMeasurement(handle);
    return em.createQuery(countQuery).getSingleResult();
  }

  @Override
  public JPAConvertableResult execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    final int handle = debugger.startRuntimeMeasurement(this, "execute");

    final List<JPAAssociationPath> orderByNaviAttributes = extractOrderByNaviAttributes();
    final List<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    final List<JPAPath> descriptionAttributes = extractDescriptionAttributes(selectionPath);
    final Map<String, From<?, ?>> joinTables = createFromClause(orderByNaviAttributes, descriptionAttributes, cq);

    cq.multiselect(createSelectClause(joinTables, selectionPath, target));

    final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
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
    result.put(ROOT_RESULT_KEY, intermediateResult);

    debugger.stopRuntimeMeasurement(handle);
    final JPANavigationProptertyInfo lastInfo = this.navigationInfo.get(this.navigationInfo.size() - 1);
    if (lastInfo.getAssociationPath() != null
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute))
      return new JPACollectionQueryResult(result, null, jpaEntity, lastInfo.getAssociationPath());
    else
      return new JPAExpandQueryResult(result, null, jpaEntity);
  }

  public List<JPANavigationProptertyInfo> getNavigationInfo() {
    return navigationInfo;
  }

  @Override
  public AbstractQuery<?> getQuery() {
    return cq;
  }

  private List<javax.persistence.criteria.Expression<?>> createGroupBy(final Map<String, From<?, ?>> joinTables,
      final List<JPAPath> selectionPathList) {
    final int handle = debugger.startRuntimeMeasurement(this, "createGroupBy");

    final List<javax.persistence.criteria.Expression<?>> groupBy =
        new ArrayList<>();

    for (final JPAPath jpaPath : selectionPathList) {
      groupBy.add(ExpressionUtil.convertToCriteriaPath(joinTables, root, jpaPath.getPath()));
    }

    debugger.stopRuntimeMeasurement(handle);
    return groupBy;
  }

  private javax.persistence.criteria.Expression<Boolean> createWhere() throws ODataApplicationException {
    return super.createWhere(uriResource, navigationInfo);
  }

  private List<JPAAssociationPath> extractOrderByNaviAttributes() throws ODataApplicationException {
    final List<JPAAssociationPath> naviAttributes = new ArrayList<>();

    final OrderByOption orderBy = uriResource.getOrderByOption();
    if (orderBy != null) {
      for (final OrderByItem orderByItem : orderBy.getOrders()) {
        final Expression expression = orderByItem.getExpression();
        if (expression instanceof Member) {
          final UriInfoResource resourcePath = ((Member) expression).getResourcePath();
          final StringBuilder pathString = new StringBuilder();
          for (final UriResource uriResource : resourcePath.getUriResourceParts()) {
            try {
              if (uriResource instanceof UriResourceNavigation) {
                final EdmNavigationProperty edmNaviProperty = ((UriResourceNavigation) uriResource).getProperty();
                naviAttributes.add(jpaEntity.getAssociationPath(edmNaviProperty.getName()));
              } else if (uriResource instanceof UriResourceProperty && ((UriResourceProperty) uriResource)
                  .isCollection()) {
                pathString.append(((UriResourceProperty) uriResource).getProperty().getName());
                naviAttributes.add(((JPACollectionAttribute) jpaEntity.getPath(pathString.toString())
                    .getLeaf()).asAssociation());

              } else if (uriResource instanceof UriResourceProperty) {
                pathString.append(((UriResourceProperty) uriResource).getProperty().getName());
                pathString.append(JPAPath.PATH_SEPERATOR);
              }
            } catch (ODataJPAModelException e) {
              throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_RESULT_CONV_ERROR,
                  HttpStatusCode.INTERNAL_SERVER_ERROR, e);
            }
          }
        }
      }
    }
    return naviAttributes;
  }

}
