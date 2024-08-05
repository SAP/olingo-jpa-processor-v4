package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.converter.JPAExpandResult.ROOT_RESULT_KEY;
import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ENTITY_UNKNOWN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.AbstractQuery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPACollectionAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.properties.JPAProcessorAttribute;

public class JPAJoinQuery extends JPAAbstractJoinQuery {

  private static List<JPANavigationPropertyInfo> determineNavigationInfo(
      final JPAServiceDocument sd, final UriInfoResource uriResource)
      throws ODataException {

    return Utility.determineNavigationPath(sd, uriResource.getUriResourceParts(), uriResource);
  }

  private static JPAEntityType determineTargetEntityType(final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    final var resources = requestContext.getUriInfo().getUriResourceParts();
    final var bindingTarget = Utility.determineBindingTarget(resources);
    if (bindingTarget instanceof EdmBoundCast) {
      return requestContext.getEdmProvider().getServiceDocument().getEntity(bindingTarget.getEntityType());
    }
    return requestContext.getEdmProvider().getServiceDocument().getEntity(bindingTarget.getName());
  }

  @Nonnull
  private static JPAEntityType determineODataTargetEntityType(final JPAODataRequestContextAccess requestContext)
      throws ODataApplicationException {

    final var resources = requestContext.getUriInfo().getUriResourceParts();
    try {
      final var bindingTarget = Utility.determineBindingTarget(resources);
      return Optional.ofNullable(requestContext.getEdmProvider().getServiceDocument() // NOSONAR
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
    entitySet = determineTargetEntitySet(requestContext);
    lastInfo.setPage(requestContext.getPage());
  }

  @Override
  public JPAConvertibleResult execute() throws ODataApplicationException {
    // Pre-process URI parameter, so they can be used at different places
    final var selectionPath = buildSelectionPathList(this.uriResource);
    try (var measurement = debugger.newMeasurement(this, "execute")) {
      final var orderByAttributes = getOrderByAttributes(uriResource.getOrderByOption());

      final var joinTables = createFromClause(orderByAttributes, selectionPath.joinedPersistent(), cq,
          lastInfo);

      cq.multiselect(createSelectClause(joinTables, selectionPath.joinedPersistent(), target, groups))
          .distinct(determineDistinct());

      final var whereClause = createWhere();
      if (whereClause != null) {
        cq.where(whereClause);
      }

      cq.orderBy(createOrderByBuilder().createOrderByList(joinTables, orderByAttributes, page));

      if (orderByAttributes.stream().anyMatch(JPAProcessorAttribute::requiresJoin)) {
        cq.groupBy(createGroupBy(joinTables, root, selectionPath.joinedPersistent(), orderByAttributes));
      }

      final TypedQuery<Tuple> typedQuery = em.createQuery(cq);
      addTopSkip(typedQuery);

      final var result = new HashMap<String, List<Tuple>>(1);
      List<Tuple> intermediateResult;
      try (var resultMeasurement = debugger.newMeasurement(this, "getResultList")) {
        intermediateResult = typedQuery.getResultList();
      }
      result.put(ROOT_RESULT_KEY, intermediateResult);
      return returnResult(selectionPath.joinedRequested(), result);
    } catch (final JPANoSelectionException e) {
      return returnEmptyResult(selectionPath.joinedRequested());
    }
  }

  private JPAOrderByBuilder createOrderByBuilder() throws ODataJPAQueryException {
    if (entitySet.isPresent()) {
      return new JPAOrderByBuilder(entitySet.get(), jpaEntity, target, cb, groups);
    }
    return new JPAOrderByBuilder(jpaEntity, target, cb, groups);
  }

  public List<JPANavigationPropertyInfo> getNavigationInfo() {
    return navigationInfo;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractQuery<Tuple> getQuery() {
    return cq;
  }

  jakarta.persistence.criteria.Expression<Boolean> createWhere() throws ODataApplicationException {

    final var filter = super.createWhere(uriResource, navigationInfo);
    return addWhereClause(filter, createProtectionWhere(claimsProvider));
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
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute)) {
      return new JPACollectionQueryResult(jpaEntity, lastInfo.getAssociationPath(), selectionPath);
    }
    return new JPAExpandQueryResult(jpaEntity, selectionPath);
  }

  private JPAConvertibleResult returnResult(@Nonnull final Collection<JPAPath> selectionPath,
      final HashMap<String, List<Tuple>> result) throws ODataApplicationException {
    final var odataEntityType = determineODataTargetEntityType(requestContext);
    if (lastInfo.getAssociationPath() != null
        && (lastInfo.getAssociationPath().getLeaf() instanceof JPACollectionAttribute)) {
      return new JPACollectionQueryResult(result, null, odataEntityType, lastInfo.getAssociationPath(), selectionPath);
    }
    return new JPAExpandQueryResult(result, Collections.emptyMap(), odataEntityType, selectionPath);
  }
}
