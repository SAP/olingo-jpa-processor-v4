package com.sap.olingo.jpa.processor.core.processor;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriInfoResource;

import com.sap.olingo.jpa.processor.core.api.JPAODataClaimProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataGroupProvider;
import com.sap.olingo.jpa.processor.core.api.JPAODataPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContext;
import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.exception.JPAIllicalAccessException;
import com.sap.olingo.jpa.processor.core.serializer.JPASerializer;

public final class JPAODataRequestContextImpl implements JPAODataRequestContext, JPAODataRequestContextAccess,
    JPARequestContext {

  private Optional<JPAODataClaimProvider> claims = Optional.empty();
  private Optional<JPAODataGroupProvider> groups = Optional.empty();
  private EntityManager em;
  private UriInfoResource uriInfo;
  private JPASerializer serializer;
  private JPAODataPage page;

  JPAODataRequestContextImpl(final JPAODataPage page, final JPASerializer serializer,
      final JPAODataRequestContextAccess context) throws JPAIllicalAccessException {
    copyContextValues(context);
    this.serializer = serializer;
    setJPAODataPage(page);

  }

  public JPAODataRequestContextImpl(final UriInfoResource uriInfo, final JPAODataRequestContextAccess context) {
    this(uriInfo, null, context);
  }

  JPAODataRequestContextImpl(final UriInfoResource uriInfo, @Nullable final JPASerializer serializer,
      final JPAODataRequestContextAccess context) {
    copyContextValues(context);
    this.serializer = serializer;
    this.uriInfo = uriInfo;
  }

  public JPAODataRequestContextImpl() {
    // Provide all data via setter
  }

  @Override
  public void setClaimsProvider(final JPAODataClaimProvider provider) {
    claims = Optional.ofNullable(provider);
  }

  @Override
  public void setGroupsProvider(final JPAODataGroupProvider provider) {
    groups = Optional.ofNullable(provider);
  }

  @Override
  public EntityManager getEntityManager() {
    return this.em;
  }

  @Override
  public UriInfoResource getUriInfo() {
    return this.uriInfo;
  }

  @Override
  public JPASerializer getSerializer() {
    return serializer;
  }

  @Override
  public JPAODataPage getPage() {
    return page;
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
  public void setEntityManager(final EntityManager em) {
    this.em = Objects.requireNonNull(em);
  }

  @Override
  public void setUriInfo(@Nonnull final UriInfo uriInfo) throws JPAIllicalAccessException {
    if (this.page != null)
      throw new JPAIllicalAccessException();
    this.uriInfo = Objects.requireNonNull(uriInfo);
  }

  @Override
  public void setJPASerializer(@Nonnull final JPASerializer serializer) {
    this.serializer = Objects.requireNonNull(serializer);
  }

  @Override
  public void setJPAODataPage(@Nonnull final JPAODataPage page) throws JPAIllicalAccessException {
    if (this.uriInfo != null)
      throw new JPAIllicalAccessException();
    this.setUriInfo(page.getUriInfo());
    this.page = Objects.requireNonNull(page);
  }

  private void copyContextValues(final JPAODataRequestContextAccess context) {
    this.claims = context.getClaimsProvider();
    this.groups = context.getGroupsProvider();
    this.em = context.getEntityManager();
  }
}
