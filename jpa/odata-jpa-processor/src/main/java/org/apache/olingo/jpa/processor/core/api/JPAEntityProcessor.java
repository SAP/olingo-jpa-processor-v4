package org.apache.olingo.jpa.processor.core.api;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceFunction;

public class JPAEntityProcessor extends JPAAbstractProcessor implements CountEntityCollectionProcessor,
    EntityProcessor {
  // TODO eliminate transaction handling
  private OData odata;
  private ServiceMetadata serviceMetadata;

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
  public final void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    if (uriResource instanceof UriResourceEntitySet) {
      EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
      EntityCollection result = readEntityInternal(request, response, uriInfo, responseFormat);
      if (result.getEntities() != null && result.getEntities().size() > 0) {
        SerializerResult serializerResult = serializeEntityResult(request, responseFormat, targetEdmEntitySet,
            result, uriInfo);
        createSuccessResonce(response, responseFormat, serializerResult);
      } else
        response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
  }

  @Override
  public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    EntityCollection result = null;
    EdmEntitySet targetEdmEntitySet = null;
    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    if (uriResource instanceof UriResourceEntitySet) {
      targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
      result = readEntityInternal(request, response, uriInfo, responseFormat);
    } else if (uriResource instanceof UriResourceFunction) {
      result = executeFunction(request, response, uriInfo, responseFormat);
    } else {
      throw new ODataApplicationException("Unsupported resource type", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(),
          Locale.ENGLISH);
    }
    if (result.getEntities() != null && result.getEntities().size() > 0) {
      SerializerResult serializerResult = serializeCollectionResult(request, responseFormat, targetEdmEntitySet,
          result, uriInfo);
      createSuccessResonce(response, responseFormat, serializerResult);
    } else
      response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());
  }

  private EntityCollection executeFunction(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException {

    UriResource uriResource = uriInfo.getUriResourceParts().get(0);
    EdmType type = ((UriResourceFunction) uriResource).getFunction().getReturnType().getType();
    if (!(uriResource instanceof UriResourceFunction)) {
      throw new ODataApplicationException("Not implemented",
          HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
    }
    return null;
  }

  /**
   * @param request
   * @param responseFormat
   * @param resultEdmEntityType
   * @param startEdmEntitySet
   * @return
   * @throws SerializerException
   */
  private SerializerResult serializeCollectionResult(ODataRequest request, ContentType responseFormat,
      EdmEntitySet resultEdmEntitySet, EntityCollection responseEntityCollection,
      UriInfo uriInfo)
          throws SerializerException {

    String selectList = odata.createUriHelper().buildContextURLSelectList(resultEdmEntitySet.getEntityType(),
        null, uriInfo.getSelectOption());

    ContextURL contextUrl = ContextURL.with()
        .entitySet(resultEdmEntitySet)
        .selectList(selectList)
        .build();

    final String id = request.getRawBaseUri() + "/" + resultEdmEntitySet.getEntityType().getName();
    EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
        .contextURL(contextUrl)
        .id(id)
        .count(uriInfo.getCountOption())
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    ODataSerializer serializer = odata.createSerializer(responseFormat);
    SerializerResult serializerResult = serializer.entityCollection(this.serviceMetadata, resultEdmEntitySet
        .getEntityType(), responseEntityCollection, opts);
    return serializerResult;
  }

  private SerializerResult serializeEntityResult(ODataRequest request, ContentType responseFormat,
      EdmEntitySet targetEdmEntitySet, EntityCollection result, UriInfo uriInfo) throws SerializerException {
    EdmEntityType entityType = targetEdmEntitySet.getEntityType();
    ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet).suffix(Suffix.ENTITY)
        .build();
    EntitySerializerOptions options = EntitySerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();
    ODataSerializer serializer = odata.createSerializer(responseFormat);
    SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, result.getEntities().get(0),
        options);
    return serializerResult;
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
