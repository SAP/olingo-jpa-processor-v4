package com.sap.olingo.jpa.processor.core.api;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

class JPADefaultPagingProvider implements JPAODataPagingProvider {

  @Override
  public Optional<JPAODataPage> getNextPage(final String skipToken, final OData odata,
      final ServiceMetadata serviceMetadata, final JPARequestParameterMap requestParameter, final EntityManager em) {
    return Optional.empty();
  }

  @Override
  public Optional<JPAODataPage> getFirstPage(final JPARequestParameterMap requestParameter,
      final JPAODataPathInformation pathInformation, final UriInfo uriInfo, final Integer preferredPageSize,
      final JPACountQuery countQuery, final EntityManager em) throws ODataApplicationException {

    final var skipValue = uriInfo.getSkipOption() != null ? uriInfo.getSkipOption().getValue() : 0;
    final var topValue = uriInfo.getTopOption() != null ? uriInfo.getTopOption().getValue() : Integer.MAX_VALUE;
    return Optional.of(new JPAODataPage(uriInfo, skipValue, topValue, null));
  }

}
