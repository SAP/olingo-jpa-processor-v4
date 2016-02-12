package org.apache.olingo.jpa.processor.core.api;

import java.util.Locale;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.processor.JPAProcessorFactory;
import org.apache.olingo.jpa.processor.core.processor.JPARequestProcessor;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.CountEntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPAEntityProcessor extends JPAAbstractProcessor implements CountEntityCollectionProcessor,
    EntityProcessor {
  // TODO eliminate transaction handling

  public JPAEntityProcessor(ServicDocument sd, EntityManager em) {
    super(sd, em);
    this.cb = em.getCriteriaBuilder();

  }

  @Override
  public void countEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    JPARequestProcessor p = new JPAProcessorFactory(odata, serviceMetadata, sd).createProcessor(em, uriInfo,
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
  public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo)
      throws ODataApplicationException, ODataLibraryException {
    throw new ODataApplicationException("Delete not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  @Override
  public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
      throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p = new JPAProcessorFactory(odata, serviceMetadata, sd).createProcessor(em, uriInfo,
        responseFormat);
    p.retrieveData(request, response, responseFormat);
  }

  @Override
  public void readEntityCollection(ODataRequest request, ODataResponse response, UriInfo uriInfo,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {

    JPARequestProcessor p = new JPAProcessorFactory(odata, serviceMetadata, sd).createProcessor(em, uriInfo,
        responseFormat);
    p.retrieveData(request, response, responseFormat);
  }

  @Override
  public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat,
      ContentType responseFormat) throws ODataApplicationException, ODataLibraryException {
    throw new ODataApplicationException("Update not implemented",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }
}
