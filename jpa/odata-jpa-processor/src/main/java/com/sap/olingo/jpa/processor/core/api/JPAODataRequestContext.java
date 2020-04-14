package com.sap.olingo.jpa.processor.core.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

import org.apache.olingo.server.api.debug.DebugSupport;

public interface JPAODataRequestContext {

  public void setClaimsProvider(@Nullable final JPAODataClaimProvider provider);

  public void setGroupsProvider(@Nullable final JPAODataGroupProvider provider);

  public void setEntityManager(@Nonnull final EntityManager em);

  public void setDebugSupport(@Nullable final DebugSupport debugSupport);

  public void setTransactionFactory(@Nullable final JPAODataTransactionFactory transactionFactory);

}
