package com.sap.olingo.jpa.processor.core.api;

import java.util.Optional;

import jakarta.persistence.EntityManager;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
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

//    final SkipOption skipOption = uriResource.getSkipOption();
//    if (skipOption != null || page != null) {
//      int skipNumber = skipOption != null ? skipOption.getValue() : page.skip();
//      skipNumber = skipOption != null && page != null ? Math.max(skipOption.getValue(), page.skip()) : skipNumber;
//      if (skipNumber >= 0)
//        typedQuery.setFirstResult(skipNumber);
//      else
//        throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
//            HttpStatusCode.BAD_REQUEST, Integer.toString(skipNumber), "$skip");
//    }

    final var skipValue = uriInfo.getSkipOption() != null ? determineSkipValue(uriInfo) : 0;
    final var topValue = uriInfo.getTopOption() != null ? determineTopValue(uriInfo) : Integer.MAX_VALUE;
    return Optional.of(new JPAODataPage(uriInfo, skipValue, topValue, null));
  }

  private int determineTopValue(final UriInfo uriInfo) throws ODataJPAQueryException {
    final var value = uriInfo.getTopOption().getValue();
    if (value < 0)
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
          HttpStatusCode.BAD_REQUEST, Integer.toString(value), "$skip");
    return value;
  }

  private int determineSkipValue(final UriInfo uriInfo) throws ODataJPAQueryException {
    final var value = uriInfo.getSkipOption().getValue();
    if (value < 0)
      throw new ODataJPAQueryException(ODataJPAQueryException.MessageKeys.QUERY_PREPARATION_INVALID_VALUE,
          HttpStatusCode.BAD_REQUEST, Integer.toString(value), "$skip");
    return value;
  }

}
