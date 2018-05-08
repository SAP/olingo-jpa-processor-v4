package com.sap.olingo.jpa.processor.core.api;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public interface JPAODataRequestContextAccess {

  public EntityManager getEntityManager();

  public UriInfo getUriInfo();

  public JPASerializer getSerializer();

  public JPAODataPage getPage();

}
