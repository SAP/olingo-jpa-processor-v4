package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public interface JPAODataRequestContextAccess {

  public @Nonnull EntityManager getEntityManager();

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

  public Optional<EdmQueryExtensionProvider> getQueryEnhancment(@Nonnull final JPAEntityType et)
      throws ODataJPAProcessorException;

  public @Nonnull JPAHttpHeaderMap getHeader();

  public @Nonnull JPARequestParameterMap getRequestParameter();

  /**
   *
   * @return most significant locale. Used e.g. for description properties
   */
  public @CheckForNull Locale getLocale();

  /**
   *
   * @return list of locales provided for this request
   */
  public List<Locale> getProvidedLocale();
}
