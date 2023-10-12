package com.sap.olingo.jpa.processor.core.api;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import jakarta.persistence.EntityManager;

import org.apache.olingo.server.api.debug.DebugSupport;

import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.processor.core.processor.JPARequestParameterHashMap;

public class JPAODataExternalRequestContext implements JPAODataRequestContext {

  private final JPARequestParameterMap customParameter;
  private final DebugSupport debugSupport;
  private final Optional<JPAODataClaimProvider> claims;
  private final Optional<JPAODataGroupProvider> groups;
  private final JPAODataTransactionFactory transactionFactory;
  private final EntityManager em;
  private final JPACUDRequestHandler cudRequestHandler;
  private final List<Locale> locales;

  public JPAODataExternalRequestContext(final Builder builder) {

    this.debugSupport = builder.debugSupport;
    this.claims = Optional.ofNullable(builder.claimsProvider);
    this.groups = Optional.ofNullable(builder.groupsProvider);
    this.transactionFactory = builder.transactionFactory;
    this.em = builder.em;
    this.cudRequestHandler = builder.cudRequestHandler;
    this.customParameter = builder.customParameter;
    this.locales = builder.locales;
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
  public List<Locale> getLocales() {
    return locales;
  }

  public static class Builder {
    public static final int CONTAINS_ONLY_LANGU = 1;
    public static final int CONTAINS_LANGU_COUNTRY = 2;
    public static final String SELECT_ITEM_SEPARATOR = ",";

    private final JPARequestParameterMap customParameter = new JPARequestParameterHashMap();
    private DebugSupport debugSupport;
    private JPAODataClaimProvider claimsProvider;
    private JPAODataGroupProvider groupsProvider;
    private JPAODataTransactionFactory transactionFactory;
    private EntityManager em;
    private JPACUDRequestHandler cudRequestHandler;
    private List<Locale> locales = emptyList();

    public JPAODataRequestContext build() {
      return new JPAODataExternalRequestContext(this);
    }

    /**
     * Adds a Claims Provider to the request context providing the claims of the current user.
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
      this.cudRequestHandler = requireNonNull(cudRequestHandler);
      return this;
    }

    /**
     * Add a request specific parameter to re request context.
     * @param name
     * @param value
     * @return
     */
    public Builder setParameter(@Nonnull final String name, @Nullable final Object value) {
      customParameter.put(requireNonNull(name), value);
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
     * An entity manager can be provided. If the entity manager is not provided, one is created automatically from the
     * entity manager factory provided by the session context {@link JPAODataServiceContext}
     * @param em
     * @return Builder
     */
    public Builder setEntityManager(@Nonnull final EntityManager em) {
      this.em = requireNonNull(em);
      return this;
    }

    /**
     * Adds a Field Group Provider to the request context providing the field groups the current user is assigned to.
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

    /**
     * Sets the locales relevant for the current request. The first locale is used e.g. for description properties. If
     * no locale is set, as a fallback the accept-language header is used.
     * @param locales
     * @return
     */
    public Builder setLocales(@Nonnull final List<Locale> locales) {
      this.locales = requireNonNull(locales);
      return this;
    }

    /**
     * Sets the locale relevant for the current request. The locale is used e.g. for description properties. If no
     * locale is set, as a fallback the accept-language header is used.
     * @param locale
     * @return
     */
    public Builder setLocales(@Nonnull final Locale locale) {
      this.locales = Collections.singletonList(requireNonNull(locale));
      return this;
    }
  }

  @Override
  public JPARequestParameterMap getRequestParameter() {
    return customParameter;
  }
}
