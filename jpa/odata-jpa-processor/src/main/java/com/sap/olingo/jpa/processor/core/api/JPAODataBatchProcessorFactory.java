package com.sap.olingo.jpa.processor.core.api;

import javax.annotation.Nonnull;

public interface JPAODataBatchProcessorFactory<T extends JPAODataBatchProcessor> {

  T getBatchProcessor(@Nonnull final JPAODataSessionContextAccess serviceContext,
      @Nonnull final JPAODataRequestContextAccess requestContext);
}
