package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.debug.DebugSupport;

import com.sap.olingo.jpa.processor.core.api.JPAODataExternalRequestContext.Builder;

public interface JPAODataRequestContext {
  public static Builder with() {
    return new Builder();
  }

  public EntityManager getEntityManager();

  public Optional<JPAODataClaimProvider> getClaimsProvider();

  public Optional<JPAODataGroupProvider> getGroupsProvider();

  public JPACUDRequestHandler getCUDRequestHandler();

  public DebugSupport getDebuggerSupport();

  public JPAODataTransactionFactory getTransactionFactory();

  @Nullable
  public Object getParameter(@Nonnull String parameterName);

  public Map<String, Object> getParameters();

  public List<Locale> getLocales();
}
