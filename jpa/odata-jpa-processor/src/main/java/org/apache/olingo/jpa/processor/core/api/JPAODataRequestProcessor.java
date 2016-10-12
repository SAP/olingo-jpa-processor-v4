package org.apache.olingo.jpa.processor.core.api;

import java.io.InputStream;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import org.apache.olingo.jpa.processor.core.processor.JPAProcessorFactory;
import org.apache.olingo.jpa.processor.core.processor.JPARequestProcessor;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

public class JPAODataRequestProcessor implements PrimitiveValueProcessor,
    ComplexProcessor, CountEntityCollectionProcessor, EntityProcessor, MediaEntityProcessor {
  private final EntityManager em;
  private final JPAODataSessionContextAccess context;
  private JPAProcessorFactory factory;
  private OData odata;
  private ServiceMetadata serviceMetadata;

  public JPAODataRequestProcessor(final JPAODataSessionContextAccess context, final EntityManager em) {
    super();
    this.em = em;
    this.context = context;
  }

  @Override
  public void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.factory = new JPAProcessorFactory(odata, serviceMetadata, context);
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public void countEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p;
    try {
      p = factory.createProcessor(em, uriInfo, ContentType.TEXT_PLAIN);
      p.retrieveData(request, response, ContentType.TEXT_PLAIN);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void createEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    EdmEntitySet edmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    EdmEntityType edmEntityType = edmEntitySet.getEntityType();

    InputStream requestInputStream = request.getBody();
    ODataDeserializer deserializer = odata.createDeserializer(requestFormat);
    DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
    Entity requestEntity = result.getEntity();

    Entity createdEntity = null;
    // Entity createdEntity = storage.createEntityData(edmEntitySet, requestEntity);

    // 3. serialize the response (we have to return the created entity)
    ContextURL contextUrl = ContextURL.with().entitySet(edmEntitySet).build();
    // expand and select currently not supported
    EntitySerializerOptions options = EntitySerializerOptions.with().contextURL(contextUrl).build();

    ODataSerializer serializer = this.odata.createSerializer(responseFormat);
    SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, options);

    // 4. configure the response object
    response.setContent(serializedResponse.getContent());
    response.setStatusCode(HttpStatusCode.CREATED.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void createMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    // 1. Retrieve the entity set which belongs to the requested entity
    List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
    // Note: only in our example we can assume that the first segment is the EntitySet
    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

    // 2. delete the data in backend
    List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
    // storage.deleteEntityData(edmEntitySet, keyPredicates);

    // 3. configure the response object
    response.setStatusCode(HttpStatusCode.NO_CONTENT.getStatusCode());

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deletePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deletePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void readComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p;
    try {
      p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }

  }

  @Override
  public void readEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void readPrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void readPrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p;
    try {
      p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }

  }

  @Override
  public void readMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(), HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(),
          null, e);
    }
  }

  @Override
  public void updateComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updateEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updatePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updatePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updateMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

}
