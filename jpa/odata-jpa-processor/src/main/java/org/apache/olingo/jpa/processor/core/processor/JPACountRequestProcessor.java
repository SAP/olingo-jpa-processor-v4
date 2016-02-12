package org.apache.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.jpa.processor.core.query.JPAQuery;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

/**
 * <a href=
 * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398314">
 * OData Version 4.0 Part 2 - 11.2.9 Requesting the Number of Items in a Collection</a>
 */
public class JPACountRequestProcessor extends JPAAbstractRequestProcessor {

  public JPACountRequestProcessor(ServicDocument sd, EntityManager em, UriInfo uriInfo, JPASerializer serializer) {
    super(sd, em, uriInfo, serializer);
  }

  @Override
  public void retrieveData(ODataRequest request, ODataResponse response, ContentType responseFormat)
      throws ODataApplicationException, SerializerException {
    UriResource uriResource = uriInfo.getUriResourceParts().get(0);

    if (uriResource instanceof UriResourceEntitySet) {
      EntityCollection result = countEntities(request, response, uriInfo);
      createSuccessResonce(response, ContentType.TEXT_PLAIN, serializer.serialize(request, result));
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
  }

  protected final EntityCollection countEntities(final ODataRequest request, final ODataResponse response,
      final UriInfo uriInfo) throws SerializerException, ODataApplicationException {

    EntityCollection entityCollection = new EntityCollection();
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(resourceParts);

    final JPAQuery query = new JPAQuery(targetEdmEntitySet, sd, uriInfo, em, request.getAllHeaders());

    entityCollection.setCount(Integer.valueOf(query.countResults().intValue()));
    return entityCollection;
  }
}
