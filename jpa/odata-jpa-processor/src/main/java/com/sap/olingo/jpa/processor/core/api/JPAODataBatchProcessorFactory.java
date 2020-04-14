package com.sap.olingo.jpa.processor.core.api;

import javax.annotation.Nonnull;

public interface JPAODataBatchProcessorFactory<T extends JPAODataBatchProcessor> {

  T getBatchProcessor(@Nonnull final JPAODataCRUDContextAccess serviceContext,
      @Nonnull final JPAODataRequestContextAccess requestContext);
}
