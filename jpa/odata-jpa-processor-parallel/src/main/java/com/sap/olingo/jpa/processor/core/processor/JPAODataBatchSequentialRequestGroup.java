package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.core.batchhandler.BatchFacadeImpl;

import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

public class JPAODataBatchSequentialRequestGroup implements JPAODataBatchRequestGroup {

  private final JPAODataParallelBatchProcessor processor;
  private final List<BatchRequestPart> groupElements;

  public JPAODataBatchSequentialRequestGroup(final JPAODataParallelBatchProcessor processor,
      final List<BatchRequestPart> groupElements) {
    this.groupElements = groupElements;
    this.processor = processor;
  }

  @Override
  public List<ODataResponsePart> execute() {
    final BatchFacade facade = buildFacade();
    return groupElements.stream()
        .map(part -> executePart(facade, part))
        .toList();
  }

  private ODataResponsePart executePart(final BatchFacade facade, final BatchRequestPart part) {
    try {
      return facade.handleBatchRequest(part);
    } catch (ODataApplicationException | ODataLibraryException e) {
      throw new ODataJPABatchRuntimeException(e);
    }
  }

  private BatchFacade buildFacade() {
    final ODataHandler odataHandler = processor.getOdata().createRawHandler(processor.getServiceMetadata());
    odataHandler.register(new JPAODataRequestProcessor(processor.getServiceContext(), processor.getRequestContext()));
    return new BatchFacadeImpl(odataHandler, processor, true);
  }
}
