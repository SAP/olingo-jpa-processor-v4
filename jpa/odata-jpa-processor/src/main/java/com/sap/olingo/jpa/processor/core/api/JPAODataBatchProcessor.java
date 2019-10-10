package com.sap.olingo.jpa.processor.core.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.OptimisticLockException;
import javax.persistence.RollbackException;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.batch.BatchFacade;
import org.apache.olingo.server.api.deserializer.batch.BatchOptions;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.apache.olingo.server.api.deserializer.batch.ODataResponsePart;
import org.apache.olingo.server.api.processor.BatchProcessor;

import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPATransactionException;

/**
 * 
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793748">
 * 11.7 Batch Requests </a>
 * 
 * @author Oliver Grande
 *
 */
public final class JPAODataBatchProcessor implements BatchProcessor {

  private final JPAODataRequestContextAccess requestContext;
  private OData odata;

  public JPAODataBatchProcessor(final JPAODataRequestContextAccess requestContext) {
    this.requestContext = requestContext;
  }

  @Override
  public void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
  }

  @Override
  public void processBatch(final BatchFacade facade, final ODataRequest request, final ODataResponse response)
      throws ODataApplicationException, ODataLibraryException {

    final int handle = requestContext.getDebugger().startRuntimeMeasurement(this, "processBatch");
    final String boundary = facade.extractBoundaryFromContentType(request.getHeader(HttpHeader.CONTENT_TYPE));
    final BatchOptions options = BatchOptions.with()
        .rawBaseUri(request.getRawBaseUri())
        .rawServiceResolutionUri(request.getRawServiceResolutionUri())
        .build();
    final List<BatchRequestPart> requestParts = odata.createFixedFormatDeserializer()
        .parseBatchRequest(request.getBody(), boundary, options);

    final List<ODataResponsePart> responseParts = new ArrayList<>();
    for (final BatchRequestPart part : requestParts) {
      responseParts.add(facade.handleBatchRequest(part));
    }
    final String responseBoundary = "batch_" + UUID.randomUUID().toString();
    final InputStream responseContent = odata.createFixedFormatSerializer().batchResponse(responseParts,
        responseBoundary);

    response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.MULTIPART_MIXED + ";boundary=" + responseBoundary);
    response.setContent(responseContent);
    response.setStatusCode(HttpStatusCode.ACCEPTED.getStatusCode());
    requestContext.getDebugger().stopRuntimeMeasurement(handle);
  }

  /**
   * Processing one change set of a $batch request. <p>
   * <i>OData Version 4.0 Part 1: Protocol Plus Errata 02 11.7.4 Responding
   * to a Batch Request</i> states: <br>
   * <cite>All operations in a change set represent a single change unit so a
   * service MUST successfully process and apply all the requests in the
   * change set or else apply none of them. It is up to the service
   * implementation to define rollback semantics to undo any requests
   * within a change set that may have been applied before another request
   * in that same change set failed and thereby apply this all-or-nothing
   * requirement. The service MAY execute the requests within a change set
   * in any order and MAY return the responses to the individual requests
   * in any order. The service MUST include the Content-ID header in each
   * response with the same value that the client specified in the
   * corresponding request, so clients can correlate requests and
   * responses.</cite>
   * <p>
   * This requires that the batch processor can create transactions. To do so it takes an instance of
   * {@link JPAODataTransactionFactory } from the request context and requests a new transaction. In case this is not
   * possible a exception with http status code 501 <i>Not Implemented</i> will be raised.
   */
  @Override
  public ODataResponsePart processChangeSet(final BatchFacade facade, final List<ODataRequest> requests)
      throws ODataApplicationException, ODataLibraryException {
    /*
     * To keep things simple, we dispatch the requests within the Change Set
     * to the other processor interfaces.
     */
    final int handle = requestContext.getDebugger().startRuntimeMeasurement(this, "processChangeSet");
    final List<ODataResponse> responses = new ArrayList<>();
    try {
      final JPAODataTransaction t = requestContext.getTransactionFactory().createTransaction();
      try {
        for (final ODataRequest request : requests) {
          // Actual request dispatching to the other processor interfaces.
          final ODataResponse response = facade.handleODataRequest(request);

          // Determine if an error occurred while executing the request.
          // Exceptions thrown by the processors get caught and result in
          // a proper OData response.
          final int statusCode = response.getStatusCode();
          if (statusCode < 400) {
            // The request has been executed successfully. Return the
            // response as a part of the change set
            responses.add(response);
          } else {
            t.rollback();
            /*
             * In addition the response must be provided as follows:
             * 
             * OData Version 4.0 Part 1: Protocol Plus Errata 02 11.7.4
             * Responding to a Batch Request
             *
             * When a request within a change set fails, the change set
             * response is not represented using the multipart/mixed
             * media type. Instead, a single response, using the
             * application/http media type and a
             * Content-Transfer-Encoding header with a value of binary,
             * is returned that applies to all requests in the change
             * set and MUST be formatted according to the Error Handling
             * defined for the particular response format.
             * 
             * This can be simply done by passing the response of the
             * failed ODataRequest to a new instance of
             * ODataResponsePart and setting the second parameter
             * "isChangeSet" to false.
             */
            requestContext.getDebugger().stopRuntimeMeasurement(handle);
            // TODO odata.continue-on-error header
            return new ODataResponsePart(response, false);
          }
        }
        requestContext.getCUDRequestHandler().validateChanges(requestContext.getEntityManager());
        t.commit();
        requestContext.getDebugger().stopRuntimeMeasurement(handle);
        return new ODataResponsePart(responses, true);
      } catch (ODataApplicationException e) {
        t.rollback();
        requestContext.getDebugger().stopRuntimeMeasurement(handle);
        throw e;
      } catch (ODataLibraryException e) {
        // The batch request is malformed or the processor implementation is not correct.
        // Throwing an exception will stop the whole batch request not only the Change Set!
        t.rollback();
        requestContext.getDebugger().stopRuntimeMeasurement(handle);
        throw e;
      } catch (RollbackException e) {
        if (e.getCause() instanceof OptimisticLockException) {
          requestContext.getDebugger().stopRuntimeMeasurement(handle);
          throw new ODataJPAProcessorException(e.getCause().getCause(), HttpStatusCode.PRECONDITION_FAILED);
        }
        requestContext.getDebugger().stopRuntimeMeasurement(handle);
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } catch (ODataJPATransactionException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.NOT_IMPLEMENTED);
    }
  }
}
