package com.sap.olingo.jpa.processor.core.api;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.core.batchhandler.BatchFacadeImpl;

import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

public class JPAODataBatchSequentialRequestGroup implements JPAODataBatchRequestGroup {

  private final JPAODataBatchProcessor processor;
  private final List<BatchRequestPart> groupElements;

  public JPAODataBatchSequentialRequestGroup(final JPAODataBatchProcessor processor,
      final List<BatchRequestPart> groupElements) {
    this.groupElements = groupElements;
    this.processor = processor;
  }

  @Override
  public List<ODataResponsePart> execute() {
    final BatchFacade facade = buildFacade();
    return groupElements.stream()
        .map(part -> executePart(facade, part))
        .collect(Collectors.toList());
  }

  private ODataResponsePart executePart(final BatchFacade facade, final BatchRequestPart part) {
    try {
      return facade.handleBatchRequest(part);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw new ODataJPABatchRuntimeException(e);
    }
  }

  private BatchFacade buildFacade() {
    final ODataHandler odataHandler = processor.odata.createRawHandler(processor.serviceMetadata);
    odataHandler.register(new JPAODataRequestProcessor(processor.serviceContext, processor.requestContext));
    return new BatchFacadeImpl(odataHandler, processor, true);
  }
}
