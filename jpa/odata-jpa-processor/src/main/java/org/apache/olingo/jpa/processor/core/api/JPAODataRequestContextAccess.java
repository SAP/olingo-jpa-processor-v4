package org.apache.olingo.jpa.processor.core.api;

import javax.persistence.EntityManager;

import org.apache.olingo.jpa.processor.core.serializer.JPASerializer;
import org.apache.olingo.server.api.uri.UriInfo;

public interface JPAODataRequestContextAccess {

  public EntityManager getEntityManager();

  public UriInfo getUriInfo();

  public JPASerializer getSerializer();

}
