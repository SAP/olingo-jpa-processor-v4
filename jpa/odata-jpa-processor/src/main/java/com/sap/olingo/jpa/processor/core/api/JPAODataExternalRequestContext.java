package com.sap.olingo.jpa.processor.core.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.debug.DebugSupport;

public class JPAODataExternalRequestContext implements JPAODataRequestContext {

  private final Map<String, Object> customParameter;
  private final DebugSupport debugSupport;
  private final Optional<JPAODataClaimProvider> claims;
  private final Optional<JPAODataGroupProvider> groups;
  private final JPAODataTransactionFactory transactionFactory;
  private final EntityManager em;
  private final JPACUDRequestHandler cudRequestHandler;

  public JPAODataExternalRequestContext(final Builder builder) {

    this.debugSupport = builder.debugSupport;
    this.claims = Optional.ofNullable(builder.claimsProvider);
    this.groups = Optional.ofNullable(builder.groupsProvider);
    this.transactionFactory = builder.transactionFactory;
    this.em = builder.em;
    this.cudRequestHandler = builder.cudRequestHandler;
    this.customParameter = builder.customParameter;
  }

  @Override
  public EntityManager getEntityManager() {
    return em;
  }

  @Override
  public Optional<JPAODataClaimProvider> getClaimsProvider() {
    return claims;
  }

  @Override
  public Optional<JPAODataGroupProvider> getGroupsProvider() {
    return groups;
  }

  @Override
  public JPACUDRequestHandler getCUDRequestHandler() {
    return cudRequestHandler;
  }

  @Override
  public DebugSupport getDebuggerSupport() {
    return debugSupport;
  }

  @Override
  public JPAODataTransactionFactory getTransactionFactory() {
    return transactionFactory;
  }

  @Override
  public Object getParameter(final String parameterName) {
    return customParameter.get(parameterName);
  }

  @Override
  public Map<String, Object> getParameters() {
    return customParameter;
  }

  public static class Builder {
    private final Map<String, Object> customParameter = new HashMap<>();
    private DebugSupport debugSupport;
    private JPAODataClaimProvider claimsProvider;
    private JPAODataGroupProvider groupsProvider;
    private JPAODataTransactionFactory transactionFactory;
    private EntityManager em;
    private JPACUDRequestHandler cudRequestHandler;

    public JPAODataRequestContext build() {
      return new JPAODataExternalRequestContext(this);
    }

    /**
     * 
     * @param provider
     * @return Builder
     */
    public Builder setClaimsProvider(@Nullable final JPAODataClaimProvider provider) {
      this.claimsProvider = provider;
      return this;
    }

    /**
     * 
     * @param cudRequestHandler
     * @return Builder
     */
    public Builder setCUDRequestHandler(@Nonnull final JPACUDRequestHandler cudRequestHandler) {
      this.cudRequestHandler = Objects.requireNonNull(cudRequestHandler);
      return this;
    }

    /**
     * 
     * @param name
     * @param value
     * @return
     */
    public Builder setParameter(@Nonnull final String name, @Nullable final Object value) {
      customParameter.put(name, value);
      return this;
    }

    /**
     * 
     * @param debugSupport
     * @return Builder
     */
    public Builder setDebugSupport(@Nullable final DebugSupport debugSupport) {
      this.debugSupport = debugSupport;
      return this;
    }

    /**
     * 
     * @param em
     * @return Builder
     */
    public Builder setEntityManager(@Nonnull final EntityManager em) {
      this.em = Objects.requireNonNull(em);
      return this;
    }

    /**
     * 
     * @param provider
     * @return Builder
     */
    public Builder setGroupsProvider(@Nullable final JPAODataGroupProvider provider) {
      this.groupsProvider = provider;
      return this;
    }

    /**
     * Sets a transaction factory. If non is provided {@link JPAODataDefaultTransactionFactory} is taken.
     * @see JPAODataTransactionFactory
     * @param transactionFactory
     * @return Builder
     */
    public Builder setTransactionFactory(@Nullable final JPAODataTransactionFactory transactionFactory) {
      this.transactionFactory = transactionFactory;
      return this;
    }
  }
}
