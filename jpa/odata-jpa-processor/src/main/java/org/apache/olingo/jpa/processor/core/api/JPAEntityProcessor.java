package org.apache.olingo.jpa.processor.core.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;

public class JPAEntityProcessor extends JPAAbstractProcessor implements CountEntityCollectionProcessor,
    EntityProcessor {
  // TODO eliminate transaction handling

  public JPAEntityProcessor(ServicDocument sd, EntityManager em) {
    super(sd, em);
    this.cb = em.getCriteriaBuilder();

  }

  /**
   * <a href=
   * "http://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part1-protocol/odata-v4.0-errata02-os-part1-protocol-complete.html#_Toc406398314">
   * OData Version 4.0 Part 2 - 11.2.9 Requesting the Number of Items in a Collection</a>
   */
  @Override
  public void countEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    if (uriResource instanceof UriResourceEntitySet) {
      EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

      EntityCollection result = countEntities(request, response, uriInfo);

      createSuccessResonce(response, ContentType.TEXT_PLAIN, new PlainTextCountResult(result));

    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
  }

  @Override
  public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  @Override
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    JPASerializer serializer = factory.createSerializer(responseFormat, uriInfo);
    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    if (uriResource instanceof UriResourceEntitySet) {
      readEntityInternal(request, response, uriInfo, responseFormat, serializer);
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
  }

  @Override
  public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPASerializer serializer = factory.createSerializer(responseFormat, uriInfo);
    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    if (uriResource instanceof UriResourceEntitySet) {
      readEntityInternal(request, response, uriInfo, responseFormat, serializer);
    } else if (uriResource instanceof UriResourceFunction) {
      executeFunction(request, response, uriInfo, responseFormat, serializer);
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }

  }

  private EntityCollection executeFunction(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat, JPASerializer serializer) throws ODataApplicationException {

    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    EdmType type = ((UriResourceFunction) uriResource).getFunction().getReturnType().getType();
    if (!(uriResource instanceof UriResourceFunction)) {
      throw new ODataApplicationException("Not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }
    return null;
  }

  @Override
  public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    // TODO Auto-generated method stub

  }

  private class PlainTextCountResult implements SerializerResult {
    private final EntityCollection result;

    public PlainTextCountResult(final EntityCollection result) {
      this.result = result;
    }

    @Override
    public InputStream getContent() {
      Integer i = result.getCount();
      return new ByteArrayInputStream(i.toString().getBytes());
    }

  }
}
