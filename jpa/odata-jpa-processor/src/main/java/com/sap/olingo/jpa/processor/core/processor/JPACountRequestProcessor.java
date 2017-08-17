package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.query.JPAQuery;
import com.sap.olingo.jpa.processor.core.query.Util;

/**
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398314">
 * OData Version 4.0 Part 2 - 11.2.9 Requesting the Number of Items in a Collection</a>
 */
public final class JPACountRequestProcessor extends JPAAbstractGetRequestProcessor {

  public JPACountRequestProcessor(final OData odata, final JPAODataSessionContextAccess context,
      final JPAODataRequestContextAccess requestContext) throws ODataException {
    super(odata, context, requestContext);
  }

  @Override
  public void retrieveData(final ODataRequest request, final ODataResponse response, final ContentType responseFormat)
      throws ODataException {
    final UriResource uriResource = uriInfo.getUriResourceParts().get(0);

    if (uriResource instanceof UriResourceEntitySet) {
      final EntityCollection result = countEntities(request, response, uriInfo);
      createSuccessResponce(response, ContentType.TEXT_PLAIN, serializer.serialize(request, result));
    } else {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_RESOURCE_TYPE,
          HttpStatusCode.NOT_IMPLEMENTED, uriResource.getKind().toString());
    }
  }

  protected final EntityCollection countEntities(final ODataRequest request, final ODataResponse response,
      final UriInfo uriInfo) throws ODataException {

    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    JPAQuery query = null;
    try {
      query = new JPAQuery(odata, targetEdmEntitySet, sessionContext, uriInfo, em, request.getAllHeaders());
    } catch (ODataJPAModelException e) {
      throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.QUERY_PREPARATION_ERROR,
          HttpStatusCode.INTERNAL_SERVER_ERROR, e);
    }

    final EntityCollection entityCollection = new EntityCollection();
    entityCollection.setCount(Integer.valueOf(query.countResults().intValue()));
    return entityCollection;
  }
}
