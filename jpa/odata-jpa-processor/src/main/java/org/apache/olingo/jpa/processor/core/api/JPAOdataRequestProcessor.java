package org.apache.olingo.jpa.processor.core.api;

import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.processor.JPAProcessorFactory;
import org.apache.olingo.jpa.processor.core.processor.JPARequestProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.ComplexProcessor;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.processor.PrimitiveValueProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPAOdataRequestProcessor implements PrimitiveValueProcessor,
    ComplexProcessor, CountEntityCollectionProcessor, EntityProcessor {
  private final EntityManager em;
  private final ServicDocument sd;
  private final JPAODataContextAccess context;
  private OData odata;
  private ServiceMetadata serviceMetadata;
  private JPAProcessorFactory factory;

  public JPAOdataRequestProcessor(JPAODataContextAccess context, EntityManager em) {
    super();
    this.em = em;
    this.sd = context.getEdmProvider().getServiceDocument();
    this.context = context;
  }

  @Override
  public void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
    this.factory = new JPAProcessorFactory(odata, serviceMetadata, context);
  }

  @Override
  public void countEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    JPARequestProcessor p = new JPAProcessorFactory(odata, serviceMetadata, context).createProcessor(em, uriInfo,
        ContentType.TEXT_PLAIN);
    p.retrieveData(request, response, ContentType.TEXT_PLAIN);
  }

  @Override
  public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    throw new ODataApplicationException("Create not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void deleteComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataApplicationException("Delete not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    throw new ODataApplicationException("Delete not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void deletePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataApplicationException("Delete not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void deletePrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {

    throw new ODataApplicationException("Delete not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void readComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
    p.retrieveData(request, response, responseFormat);
  }

  @Override
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
    p.retrieveData(request, response, responseFormat);
  }

  @Override
  public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
    p.retrieveData(request, response, responseFormat);
  }

  @Override
  public void readPrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p = factory.createProcessor(em, uriInfo, responseFormat);
    p.retrieveData(request, response, responseFormat);
  }

  @Override
  public void readPrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    throw new ODataApplicationException("Read Primitive Value not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);

  }

  @Override
  public void updateComplex(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    throw new ODataApplicationException("Update not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    throw new ODataApplicationException("Update not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void updatePrimitive(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    throw new ODataApplicationException("Update not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void updatePrimitiveValue(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType requestFormat, ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    throw new ODataApplicationException("Update not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }
}
