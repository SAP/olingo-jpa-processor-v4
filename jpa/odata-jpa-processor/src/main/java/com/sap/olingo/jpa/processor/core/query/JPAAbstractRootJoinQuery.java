package com.sap.olingo.jpa.processor.core.query;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_ENTITY_UNKNOWN;
import static org.apache.olingo.commons.api.http.HttpStatusCode.INTERNAL_SERVER_ERROR;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.AbstractQuery;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

abstract class JPAAbstractRootJoinQuery extends JPAAbstractJoinQuery {

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
  protected static JPAEntityType determineODataTargetEntityType(final JPAODataRequestContextAccess requestContext)
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

  JPAAbstractRootJoinQuery(final OData odata, final JPAEntityType entityType,
      final JPAODataRequestContextAccess requestContext, final List<JPANavigationPropertyInfo> determineNavigationInfo)
      throws ODataException {
    super(odata, entityType, requestContext, determineNavigationInfo);
  }

  JPAAbstractRootJoinQuery(final OData odata, final JPAODataRequestContextAccess requestContext) throws ODataException {
    super(odata, determineTargetEntityType(requestContext),
        requestContext, determineNavigationInfo(requestContext.getEdmProvider().getServiceDocument(), requestContext
            .getUriInfo()));
    entitySet = determineTargetEntitySet(requestContext);
  }

  @SuppressWarnings("unchecked")
  @Override
  public AbstractQuery<Tuple> getQuery() {
    return cq;
  }

  protected jakarta.persistence.criteria.Expression<Boolean> createWhere() throws ODataApplicationException {
  
    final var filter = super.createWhere(uriResource, navigationInfo);
    return addWhereClause(filter, createProtectionWhere(claimsProvider));
  }

}
