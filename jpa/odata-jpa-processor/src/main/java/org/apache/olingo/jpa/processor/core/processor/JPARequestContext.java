package org.apache.olingo.jpa.processor.core.processor;

import javax.persistence.EntityManager;

import org.apache.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPARequestContext implements JPAODataRequestContextAccess {
  private final EntityManager em;
  private final UriInfo uriInfo;
  private final JPASerializer serializer;

  public JPARequestContext(final EntityManager em, final UriInfo uriInfo, final JPASerializer getSerializer) {
    super();
    this.em = em;
    this.uriInfo = uriInfo;
    this.serializer = getSerializer;
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public UriInfo getUriInfo() {
    return uriInfo;
  }

  @Override
  public JPASerializer getSerializer() {
    return serializer;
  }

}
