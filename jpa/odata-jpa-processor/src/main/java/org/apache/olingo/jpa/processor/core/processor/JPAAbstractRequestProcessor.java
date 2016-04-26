package org.apache.olingo.jpa.processor.core.processor;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;

abstract class JPAAbstractRequestProcessor implements JPARequestProcessor {

  // TODO eliminate transaction handling
  protected final EntityManager em;
  protected final ServicDocument sd;
  protected final JPAODataSessionContextAccess context;
  protected final CriteriaBuilder cb;
  protected final UriInfo uriInfo;
  protected final JPASerializer serializer;
  protected final OData odata;

  public JPAAbstractRequestProcessor(final OData odata, final JPAODataSessionContextAccess context,
      final JPAODataRequestContextAccess requestContext) {

    this.em = requestContext.getEntityManager();
    this.cb = em.getCriteriaBuilder();
    this.context = context;
    this.sd = context.getEdmProvider().getServiceDocument();
    this.uriInfo = requestContext.getUriInfo();
    this.serializer = requestContext.getSerializer();
    this.odata = odata;
  }

  protected final void createSuccessResonce(final ODataResponse response, final ContentType responseFormat,
      final SerializerResult serializerResult) {

    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }
}
