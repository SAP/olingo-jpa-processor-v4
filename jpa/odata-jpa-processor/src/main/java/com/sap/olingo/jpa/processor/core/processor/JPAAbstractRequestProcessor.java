package com.sap.olingo.jpa.processor.core.processor;

import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

abstract class JPAAbstractRequestProcessor {

  protected final EntityManager em;
  protected final JPAServiceDocument sd;
  protected final CriteriaBuilder cb;
  protected final UriInfoResource uriInfo;
  protected final JPASerializer serializer;
  protected final OData odata;
  protected final JPAServiceDebugger debugger;
  protected int successStatusCode = HttpStatusCode.OK.getStatusCode();
  protected final JPAODataRequestContextAccess requestContext;

  JPAAbstractRequestProcessor(final OData odata, final JPAODataRequestContextAccess requestContext)
      throws ODataException {

    this.em = requestContext.getEntityManager();
    this.cb = em.getCriteriaBuilder();
    this.sd = requestContext.getEdmProvider().getServiceDocument();
    this.uriInfo = requestContext.getUriInfo();
    this.serializer = requestContext.getSerializer();
    this.odata = odata;
    this.debugger = requestContext.getDebugger();
    this.requestContext = requestContext;
  }

  protected final void createSuccessResponse(final ODataResponse response, final ContentType responseFormat,
      final SerializerResult serializerResult, @Nullable final EntityCollection entityCollection) {

    response.setContent(serializerResult.getContent());
    response.setStatusCode(successStatusCode);
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
    createETagHeader(response, entityCollection);
  }

  protected final void createNotModifiedResponse(final ODataResponse response,
      final EntityCollection entityCollection) {
    response.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
    createETagHeader(response, entityCollection);
  }

  protected final void createPreconditionFailedResponse(final ODataResponse response) {
    response.setStatusCode(HttpStatusCode.PRECONDITION_FAILED.getStatusCode());
  }

  private void createETagHeader(final ODataResponse response, final EntityCollection entityCollection) {
    if (entityCollection != null && entityCollection.getEntities().size() == 1) {
      final var etag = entityCollection.getEntities().get(0).getETag();
      if (etag != null)
        response.setHeader(HttpHeader.ETAG, etag);
    }
  }
}