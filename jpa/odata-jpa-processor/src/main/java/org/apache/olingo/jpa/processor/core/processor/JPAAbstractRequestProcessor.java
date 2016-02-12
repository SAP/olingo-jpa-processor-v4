package org.apache.olingo.jpa.processor.core.processor;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.ServicDocument;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;

abstract class JPAAbstractRequestProcessor implements JPARequestProcessor {

  // TODO eliminate transaction handling
  protected final EntityManager em;
  protected final ServicDocument sd;
  protected final CriteriaBuilder cb;
  protected final UriInfo uriInfo;
  protected final JPASerializer serializer;

  public JPAAbstractRequestProcessor(ServicDocument sd, EntityManager em, UriInfo uriInfo, JPASerializer serializer) {
    super();
    this.em = em;
    this.cb = em.getCriteriaBuilder();
    this.sd = sd;
    this.uriInfo = uriInfo;
    this.serializer = serializer;
  }

  protected final void createSuccessResonce(ODataResponse response, ContentType responseFormat,
      SerializerResult serializerResult) {
    response.setContent(serializerResult.getContent());
    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
  }
}
