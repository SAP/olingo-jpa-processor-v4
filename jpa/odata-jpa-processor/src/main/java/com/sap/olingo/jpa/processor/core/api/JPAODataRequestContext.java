package com.sap.olingo.jpa.processor.core.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;

public interface JPAODataRequestContext {

  public void setClaimsProvider(@Nullable final JPAODataClaimProvider provider);

  public void setGroupsProvider(@Nullable final JPAODataGroupProvider provider);

  public void setEntityManager(@Nonnull final EntityManager em);
}
