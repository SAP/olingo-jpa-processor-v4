package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public interface JPAODataRequestContextAccess extends JPAODataRequestParameterAccess {

  public EntityManager getEntityManager();

  public UriInfoResource getUriInfo();

  public JPASerializer getSerializer();

  public JPAODataPage getPage();

  public Optional<JPAODataClaimProvider> getClaimsProvider();

  public Optional<JPAODataGroupProvider> getGroupsProvider();

  public JPACUDRequestHandler getCUDRequestHandler();

  public JPAServiceDebugger getDebugger();

  public JPAODataTransactionFactory getTransactionFactory();

  public Optional<EdmTransientPropertyCalculator<?>> getCalculator(@Nonnull final JPAAttribute transientProperty)
      throws ODataJPAProcessorException;

  public Map<String, List<String>> getHeader();

}
