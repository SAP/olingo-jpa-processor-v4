package com.sap.olingo.jpa.processor.core.processor;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

abstract class JPAAbstractRequestProcessor {

  protected final EntityManager em;
  protected final JPAServiceDocument sd;
  protected final JPAODataSessionContextAccess sessionContext;
  protected final CriteriaBuilder cb;
  protected final UriInfo uriInfo;
  protected final JPASerializer serializer;
  protected final OData odata;
  protected final JPAServiceDebugger debugger;
  protected int successStatusCode = HttpStatusCode.OK.getStatusCode();

  public JPAAbstractRequestProcessor(final OData odata, final JPAODataSessionContextAccess context,
      final JPAODataRequestContextAccess requestContext) throws ODataException {

    this.em = requestContext.getEntityManager();
    this.cb = em.getCriteriaBuilder();
    this.sessionContext = context;
    this.sd = context.getEdmProvider().getServiceDocument();
    this.uriInfo = requestContext.getUriInfo();
    this.serializer = requestContext.getSerializer();
    this.odata = odata;
    this.debugger = context.getDebugger();
  }

  protected final void createSuccessResponce(final ODataResponse response, final ContentType responseFormat,
      final SerializerResult serializerResult) {

    response.setContent(serializerResult.getContent());
    response.setStatusCode(successStatusCode);
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }
}
