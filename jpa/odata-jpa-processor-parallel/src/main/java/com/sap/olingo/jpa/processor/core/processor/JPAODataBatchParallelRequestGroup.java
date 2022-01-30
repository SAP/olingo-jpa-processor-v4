package com.sap.olingo.jpa.processor.core.processor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.core.batchhandler.BatchFacadeImpl;

import com.sap.olingo.jpa.processor.core.api.JPAODataRequestProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPABatchRuntimeException;

class JPAODataBatchParallelRequestGroup implements JPAODataBatchRequestGroup {
  private final List<BatchRequestPart> requestParts;
  private final JPAODataParallelBatchProcessor processor;

  JPAODataBatchParallelRequestGroup(final JPAODataParallelBatchProcessor processor,
      final List<BatchRequestPart> requestParts) {
    super();
    this.requestParts = requestParts;
    this.processor = processor;
  }

  @Override
  public List<ODataResponsePart> execute() {
    try {

      processor.getRequestContext().getDebugger().debug(this, "Number of groups elements : %d", requestParts.size());

      final List<CompletableFuture<ODataResponsePart>> requests = requestParts.stream()
          .map(part -> startBatchPart(buildFacade(), part))
          .collect(Collectors.toList());

      return CompletableFuture.allOf(requests.toArray(new CompletableFuture[requests.size()]))
          .thenApply(dummy -> requests.stream()
              .map(CompletableFuture::join)
              .collect(Collectors.toList())).join();
    } catch (RuntimeException e) {
      // startBatchPart throws an runtime exception that wraps the original exception. This runtime exception gets is
      // wrapped into an CompletionException. The original exception has to be re-wrapped, so the caller can handle it.
      throw new ODataJPABatchRuntimeException((ODataException) e.getCause().getCause());
    }
  }

  private BatchFacade buildFacade() {
    final ODataHandler odataHandler = processor.getOdata().createRawHandler(processor.getServiceMetadata());
    odataHandler.register(new JPAODataRequestProcessor(processor.getServiceContext(), processor.getRequestContext()));
    return new BatchFacadeImpl(odataHandler, processor, true);
  }

  private CompletableFuture<ODataResponsePart> startBatchPart(final BatchFacade facade,
      final BatchRequestPart requestPart) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        return facade.handleBatchRequest(requestPart);
      } catch (ODataApplicationException | ODataLibraryException e) {
        throw new ODataJPABatchRuntimeException(e);
      }
    });
  }
}
