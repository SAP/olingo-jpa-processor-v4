package com.sap.olingo.jpa.processor.core.api;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public interface JPAODataRequestContextAccess {

  public EntityManager getEntityManager();

  public UriInfoResource getUriInfo();

  public JPASerializer getSerializer();

  public JPAODataPage getPage();

  public Optional<JPAODataClaimProvider> getClaimsProvider();

  public Optional<JPAODataGroupProvider> getGroupsProvider();

}
