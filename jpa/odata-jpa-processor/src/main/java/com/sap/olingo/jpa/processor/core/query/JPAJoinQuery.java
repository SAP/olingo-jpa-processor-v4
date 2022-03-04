package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ENTITY_UNKNOWN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

public class JPAJoinQuery extends JPAAbstractJoinQuery implements JPACountQuery {

  private static List<JPANavigationPropertyInfo> determineNavigationInfo(
      final JPAServiceDocument sd, final UriInfoResource uriResource) throws ODataException {

    return Util.determineNavigationPath(sd, uriResource.getUriResourceParts(), uriResource);
  }

  private static JPAEntityType determineTargetEntityType(final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    final List<UriResource> resources = requestContext.getUriInfo().getUriResourceParts();
    final EdmBindingTarget bindingTarget = Util.determineBindingTarget(resources);
    if (bindingTarget instanceof EdmBoundCast)
      return requestContext.getEdmProvider().getServiceDocument().getEntity(bindingTarget.getEntityType());
    return requestContext.getEdmProvider().getServiceDocument().getEntity(bindingTarget.getName());
  }

  @Nonnull
  private static JPAEntityType determineODataTargetEntityType(final JPAODataRequestContextAccess requestContext)
      throws ODataApplicationException {

    final List<UriResource> resources = requestContext.getUriInfo().getUriResourceParts();
    try {
      final EdmBindingTarget bindingTarget = Util.determineBindingTarget(resources);
      return Optional.ofNullable(requestContext.getEdmProvider().getServiceDocument()
          .getEntity(bindingTarget.getEntityType()))
          .orElseThrow(() -> new ODataJPAQueryException(QUERY_PREPARATION_ENTITY_UNKNOWN, INTERNAL_SERVER_ERROR,
              bindingTarget.getEntityType().getName()));
    } catch (final ODataException e) {
      throw new ODataJPAQueryException(e, INTERNAL_SERVER_ERROR);
    }
  }

  public JPAJoinQuery(final OData odata, final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    super(odata, determineTargetEntityType(requestContext),
        requestContext, determineNavigationInfo(requestContext.getEdmProvider().getServiceDocument(), requestContext
            .getUriInfo()));
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
    final CriteriaQuery<Number> countQuery = cb.createQuery(Number.class);
    try {
      createFromClause(Collections.emptyList(), Collections.emptyList(), countQuery, lastInfo);

      final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
      if (whereClause != null)
        countQuery.where(whereClause);
      countQuery.select(cb.countDistinct(target));
      return em.createQuery(countQuery).getSingleResult().longValue();
    } catch (final JPANoSelectionException e) {
      return 0L;
    } finally {
      debugger.stopRuntimeMeasurement(handle);
    }
  }

  @Override
  public JPAConvertibleResult execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    final int handle = debugger.startRuntimeMeasurement(this, "execute");

    final List<JPAAssociationPath> orderByNaviAttributes = extractOrderByNaviAttributes(uriResource.getOrderByOption());
    final SelectionPathInfo<JPAPath> selectionPath = buildSelectionPathList(this.uriResource);
    try {
      final Map<String, From<?, ?>> joinTables = createFromClause(orderByNaviAttributes,
          selectionPath.joinedPersistent(), cq, lastInfo);

      cq.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), target, groups))
          .distinct(determineDistinct());

      final javax.persistence.criteria.Expression<Boolean> whereClause = createWhere();
      if (whereClause != null)
        cq.where(whereClause);

      cq.orderBy(new JPAOrderByBuilder(jpaEntity, target, cb, groups).createOrderByList(joinTables, uriResource));

      if (!orderByNaviAttributes.isEmpty())
        cq.groupBy(createGroupBy(joinTables, root, selectionPath.joinedPersistent()));

      final TypedQuery<Tuple> tq = em.createQuery(cq);
      addTopSkip(tq);

      final HashMap<String, List<Tuple>> result = new HashMap<>(1);
      final int resultHandle = debugger.startRuntimeMeasurement(tq, "getResultList");
      final List<Tuple> intermediateResult = tq.getResultList();

      debugger.stopRuntimeMeasurement(resultHandle);
      result.put(ROOT_RESULT_KEY, intermediateResult);
      return returnResult(selectionPath.joinedRequested(), result);
    } catch (final JPANoSelectionException e) {
      return returnEmptyResult(selectionPath.joinedRequested());
    } finally {
      debugger.stopRuntimeMeasurement(handle);
    }
  }

  public List<JPANavigationPropertyInfo> getNavigationInfo() {
    return navigationInfo;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractQuery<Tuple> getQuery() {
    return cq;
  }

  private javax.persistence.criteria.Expression<Boolean> createWhere() throws ODataApplicationException {
    return addWhereClause(super.createWhere(uriResource, navigationInfo), createProtectionWhere(claimsProvider));
  }

  /**
   * Desired if SELECT DISTINCT shall be generated. This is required e.g. if multiple values for the same claims are
   * present. As a DISTINCT is usually slower the decision algorithm my need to be enhanced in the future
   * @return
   */
  private boolean determineDistinct() {
    return claimsProvider.isPresent();
  }

  private JPAConvertibleResult returnEmptyResult(final Collection<JPAPath> selectionPath) {
    if (lastInfo.getAssociationPath() != null
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute))
      return new JPACollectionQueryResult(jpaEntity, lastInfo.getAssociationPath(), selectionPath);
    return new JPAExpandQueryResult(jpaEntity, selectionPath);
  }

  private JPAConvertibleResult returnResult(@Nonnull final Collection<JPAPath> selectionPath,
      final HashMap<String, List<Tuple>> result) throws ODataApplicationException {
    final JPAEntityType odataEntityType = determineODataTargetEntityType(requestContext);
    if (lastInfo.getAssociationPath() != null
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute))
      return new JPACollectionQueryResult(result, null, odataEntityType, lastInfo.getAssociationPath(), selectionPath);
    return new JPAExpandQueryResult(result, Collections.emptyMap(), odataEntityType, selectionPath);
  }
}
