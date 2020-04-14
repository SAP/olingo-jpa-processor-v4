package com.sap.olingo.jpa.processor.core.api;

import javax.annotation.Nonnull;

public class JPAODataParallelBatchProcessorFactory implements
    JPAODataBatchProcessorFactory<JPAODataParallelBatchProcessor> {

  @Override
  public JPAODataParallelBatchProcessor getBatchProcessor(@Nonnull final JPAODataCRUDContextAccess serviceContext,
      @Nonnull final JPAODataRequestContextAccess requestContext) {
    return new JPAODataParallelBatchProcessor(serviceContext, requestContext);
  }

}
