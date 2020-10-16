package com.sap.olingo.jpa.processor.core.api;

import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ActionPrimitiveProcessor;
import org.apache.olingo.server.api.processor.ActionVoidProcessor;
import org.apache.olingo.server.api.processor.ComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.CountComplexCollectionProcessor;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.MediaEntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveCollectionProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.processor.JPAActionRequestProcessor;
import com.sap.olingo.jpa.processor.core.processor.JPACUDRequestProcessor;
import com.sap.olingo.jpa.processor.core.processor.JPAProcessorFactory;
import com.sap.olingo.jpa.processor.core.processor.JPARequestProcessor;

public final class JPAODataRequestProcessor
    implements PrimitiveValueProcessor, PrimitiveCollectionProcessor, ComplexProcessor, ComplexCollectionProcessor,
    CountEntityCollectionProcessor, EntityProcessor, MediaEntityProcessor, ActionPrimitiveProcessor,
    ActionVoidProcessor, CountComplexCollectionProcessor {

  private final JPAODataSessionContextAccess sessionContext;
  private final JPAODataRequestContextAccess requestContext;
  private JPAProcessorFactory factory;

  public JPAODataRequestProcessor(final JPAODataSessionContextAccess sessionContext,
      final JPAODataRequestContextAccess requestContext) {
    super();
    this.sessionContext = sessionContext;
    this.requestContext = requestContext;
  }

  @Override
  public void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.factory = new JPAProcessorFactory(odata, serviceMetadata, sessionContext);
  }

  @Override
  public void countEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, ContentType.TEXT_PLAIN, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, ContentType.TEXT_PLAIN);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void countComplexCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_COUNT,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void createEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat) throws ODataApplicationException,
      ODataLibraryException {

    try {
      final JPACUDRequestProcessor p = factory.createCUDRequestProcessor(uriInfo, responseFormat, requestContext,
          request.getAllHeaders());
      p.createEntity(request, response, requestFormat, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void createMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_CREATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void deleteComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: .../Organizations('4')/Address

    try {
      final JPACUDRequestProcessor p = factory.createCUDRequestProcessor(uriInfo, requestContext, request
          .getAllHeaders());
      p.clearFields(request, response);
    } catch (ODataApplicationException | ODataLibraryException e) {
      if (e.getCause() instanceof RollbackException)
        handleRollbackException((RollbackException) e.getCause());
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void deleteEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPACUDRequestProcessor p = this.factory.createCUDRequestProcessor(uriInfo, requestContext, request
          .getAllHeaders());
      p.deleteEntity(request, response);
    } catch (ODataApplicationException | ODataLibraryException e) {
      if (e.getCause() instanceof RollbackException)
        handleRollbackException((RollbackException) e.getCause());
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void deletePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: .../Organizations('4')/Address/Country
    // https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306
    // 11.4.9.2 Set a Value to Null:
    // A successful DELETE request to the edit URL for a structural property, or to the edit URL of the raw value of a
    // primitive property, sets the property to null. The request body is ignored and should be empty. A DELETE request
    // to a non-nullable value MUST fail and the service respond with 400 Bad Request or other appropriate error. The
    // same rules apply whether the target is the value of a regular property or the value of a dynamic property. A
    // missing dynamic property is defined to be the same as a dynamic property with value null. All dynamic properties
    // are nullable.On success, the service MUST respond with 204 No Content and an empty body.
    //
    // Nullable checked by Olingo Core
    try {
      final JPACUDRequestProcessor p = factory.createCUDRequestProcessor(uriInfo, requestContext, request
          .getAllHeaders());
      p.clearFields(request, response);
    } catch (ODataApplicationException | ODataLibraryException e) {
      if (e.getCause() instanceof RollbackException)
        handleRollbackException((RollbackException) e.getCause());
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void deletePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // .../Organizations('4')/Address/Country/$value
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE_VALUE,
        HttpStatusCode.NOT_IMPLEMENTED);

  }

  @Override
  public void deleteMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: ../$value
    // https://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752305
    // 11.4.8.2 Deleting Stream Values:
    // A successful DELETE request to the edit URL of a stream property
    // attempts to set the property to null and results
    // in an error if the property is non-nullable. Attempting to request a
    // stream property whose value is null results
    // in 204 No Content.
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_DELETE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void readComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void readComplexCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void readEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void readEntityCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void readPrimitiveCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void readPrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void readPrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }

  }

  @Override
  public void readMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    try {
      final JPARequestProcessor p = factory.createProcessor(uriInfo, responseFormat, request.getAllHeaders(),
          requestContext);
      p.retrieveData(request, response, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void updateComplex(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {
    // ../Organizations('5')/Address
    // Not supported yet, as PATCH and PUT are allowed here
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE_VALUE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updateEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPACUDRequestProcessor p = factory.createCUDRequestProcessor(uriInfo, responseFormat, requestContext,
          request.getAllHeaders());
      p.updateEntity(request, response, requestFormat, responseFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      if (e.getCause() instanceof RollbackException)
        handleRollbackException((RollbackException) e.getCause());
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  private void handleRollbackException(final RollbackException e) throws ODataJPAProcessorException {
    if (e.getCause() instanceof OptimisticLockException) {
      throw new ODataJPAProcessorException(e.getCause().getCause(), HttpStatusCode.PRECONDITION_FAILED);
    }
    throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

  @Override
  public void updatePrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {
    // http://docs.oasis-open.org/odata/odata/v4.0/errata03/os/complete/part1-protocol/odata-v4.0-errata03-os-part1-protocol-complete.html#_Toc453752306
    // only PUT ../Organizations('5')/Address/StreetName
    updateEntity(request, response, uriInfo, requestFormat, responseFormat);
  }

  @Override
  public void updatePrimitiveValue(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {
    // ../Organizations('5')/Address/StreetName/$value
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE_VALUE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updateMediaEntity(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_UPDATE,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public void updatePrimitiveCollection(final ODataRequest request, final ODataResponse response,
      final UriInfo uriInfo, final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    updateEntity(request, response, uriInfo, requestFormat, responseFormat);
  }

  @Override
  public void deletePrimitiveCollection(final ODataRequest request, final ODataResponse response,
      final UriInfo uriInfo) throws ODataApplicationException, ODataLibraryException {
    // Set NULL: .../Organizations('4')/Comment
    // See deletePrimitive
    try {
      final JPACUDRequestProcessor p = factory.createCUDRequestProcessor(uriInfo, requestContext, request
          .getAllHeaders());
      p.clearFields(request, response);
    } catch (ODataApplicationException | ODataLibraryException e) {
      if (e.getCause() instanceof RollbackException)
        handleRollbackException((RollbackException) e.getCause());
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void updateComplexCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    updateEntity(request, response, uriInfo, requestFormat, responseFormat);
  }

  @Override
  public void deleteComplexCollection(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    // Set NULL: .../Persons('4')/InhouseAddress
    // See deletePrimitive
    try {
      final JPACUDRequestProcessor p = factory.createCUDRequestProcessor(uriInfo, requestContext, request
          .getAllHeaders());
      p.clearFields(request, response);
    } catch (ODataApplicationException | ODataLibraryException e) {
      if (e.getCause() instanceof RollbackException)
        handleRollbackException((RollbackException) e.getCause());
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void processActionPrimitive(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat, final ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    try {
      final JPAActionRequestProcessor p = this.factory.createActionProcessor(uriInfo, responseFormat, request
          .getAllHeaders(), requestContext);
      p.performAction(request, response, requestFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }
  }

  @Override
  public void processActionVoid(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestFormat) throws ODataApplicationException, ODataLibraryException {
    try {
      final JPAActionRequestProcessor p = this.factory.createActionProcessor(uriInfo, null, request.getAllHeaders(),
          requestContext);
      p.performAction(request, response, requestFormat);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw e;
    } catch (final ODataException e) {
      throw new ODataApplicationException(e.getLocalizedMessage(),
          HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), null, e);
    }

  }
}
