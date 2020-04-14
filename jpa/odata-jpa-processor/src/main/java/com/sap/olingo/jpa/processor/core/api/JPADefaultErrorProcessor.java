package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ODataServerError;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.DefaultProcessor;
import org.apache.olingo.server.api.processor.ErrorProcessor;

public final class JPADefaultErrorProcessor implements ErrorProcessor {
  private final ErrorProcessor defaultProcessor;

  JPADefaultErrorProcessor() {
    super();
    defaultProcessor = new DefaultProcessor();

  }

  @Override
  public void init(OData odata, ServiceMetadata serviceMetadata) {
    defaultProcessor.init(odata, serviceMetadata);

  }

  @Override
  public void processError(ODataRequest request, ODataResponse response, ODataServerError serverError,
      ContentType responseFormat) {
    defaultProcessor.processError(request, response, serverError, responseFormat);
  }

}
