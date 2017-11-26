package com.sap.olingo.jpa.processor.core.processor;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public final class JPARequestContext implements JPAODataRequestContextAccess {
  private final EntityManager em;
  private final UriInfo uriInfo;
  private final JPASerializer serializer;

  public JPARequestContext(final EntityManager em, final UriInfo uriInfo, final JPASerializer serializer) {
    super();
    this.em = em;
    this.uriInfo = uriInfo;
    this.serializer = serializer;
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
