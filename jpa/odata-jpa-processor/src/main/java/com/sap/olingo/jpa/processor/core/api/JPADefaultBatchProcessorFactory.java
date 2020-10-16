package com.sap.olingo.jpa.processor.core.api;

public class JPADefaultBatchProcessorFactory implements JPAODataBatchProcessorFactory<JPAODataBatchProcessor> {

  @Override
  public JPAODataBatchProcessor getBatchProcessor(final JPAODataSessionContextAccess serviceContext,
      final JPAODataRequestContextAccess requestContext) {

    return new JPAODataBatchProcessor(serviceContext, requestContext);
  }
}
