package com.sap.olingo.jpa.processor.core.api;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.processor.core.processor.JPAODataParallelBatchProcessor;

public class JPAODataParallelBatchProcessorFactory implements
    JPAODataBatchProcessorFactory<JPAODataParallelBatchProcessor> {

  @Override
  public JPAODataParallelBatchProcessor getBatchProcessor(@Nonnull final JPAODataSessionContextAccess serviceContext,
      @Nonnull final JPAODataRequestContextAccess requestContext) {
    return new JPAODataParallelBatchProcessor(serviceContext, requestContext);
  }

}
