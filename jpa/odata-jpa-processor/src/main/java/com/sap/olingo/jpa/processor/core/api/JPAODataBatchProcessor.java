package com.sap.olingo.jpa.processor.core.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.format.PreferenceName;
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
import org.apache.olingo.server.api.prefer.Preferences;
import org.apache.olingo.server.api.processor.BatchProcessor;

import com.sap.olingo.jpa.processor.core.api.JPAODataTransactionFactory.JPAODataTransaction;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurement;
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
public class JPAODataBatchProcessor implements BatchProcessor {

  protected final JPAODataRequestContextAccess requestContext;
  protected final JPAODataSessionContextAccess serviceContext;
  protected OData odata;
  protected ServiceMetadata serviceMetadata;

  public JPAODataBatchProcessor(final JPAODataSessionContextAccess serviceContext,
      final JPAODataRequestContextAccess requestContext) {
    this.requestContext = requestContext;
    this.serviceContext = serviceContext;
  }

  @Override
  public final void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public final void processBatch(final BatchFacade facade, final ODataRequest request, final ODataResponse response)
      throws ODataApplicationException, ODataLibraryException {

    try (JPARuntimeMeasurement measurement = requestContext.getDebugger().newMeasurement(this, "processBatch")) {
      final String boundary = facade.extractBoundaryFromContentType(request.getHeader(HttpHeader.CONTENT_TYPE));
      final BatchOptions options = BatchOptions.with()
          .rawBaseUri(request.getRawBaseUri())
          .rawServiceResolutionUri(request.getRawServiceResolutionUri())
          .build();
      final List<BatchRequestPart> requestParts = odata.createFixedFormatDeserializer()
          .parseBatchRequest(request.getBody(), boundary, options);
      final List<ODataResponsePart> responseParts = executeBatchParts(facade, requestParts,
          continueOnError(odata.createPreferences(request.getHeaders(HttpHeader.PREFER))));

      final String responseBoundary = "batch_" + UUID.randomUUID().toString();
      final InputStream responseContent = odata.createFixedFormatSerializer().batchResponse(responseParts,
          responseBoundary);

      response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.MULTIPART_MIXED + ";boundary=" + responseBoundary);
      response.setContent(responseContent);
      response.setStatusCode(HttpStatusCode.ACCEPTED.getStatusCode());
    }
  }

  protected List<ODataResponsePart> executeBatchParts(final BatchFacade facade,
      final List<BatchRequestPart> requestParts, final boolean continueOnError) throws ODataApplicationException,
      ODataLibraryException {

    final List<ODataResponsePart> responseParts = new ArrayList<>(requestParts.size());
    for (final BatchRequestPart part : requestParts) {
      final ODataResponsePart resp = facade.handleBatchRequest(part);
      responseParts.add(resp);
      final List<ODataResponse> responses = resp.getResponses();
      responses.get(responses.size() - 1).getStatusCode();
      if (requestHasFailed(responses) && !continueOnError)
        return responseParts;
    }
    return responseParts;
  }

  /**
   * Processing one change set of a $batch request.
   * <p>
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
  public final ODataResponsePart processChangeSet(final BatchFacade facade, final List<ODataRequest> requests)
      throws ODataApplicationException, ODataLibraryException {
    /*
     * To keep things simple, we dispatch the requests within the Change Set
     * to the other processor interfaces.
     */
    final List<ODataResponse> responses = new ArrayList<>();
    try (JPARuntimeMeasurement measurement = requestContext.getDebugger().newMeasurement(this, "processChangeSet")) {
      final JPAODataTransaction t = requestContext.getTransactionFactory().createTransaction();
      try {
        for (final ODataRequest request : requests) {
          // Actual request dispatching to the other processor interfaces.
          final ODataResponse response = facade.handleODataRequest(request);

          // Determine if an error occurred while executing the request.
          // Exceptions thrown by the processors get caught and result in
          // a proper OData response.
          final int statusCode = response.getStatusCode();
          if (statusCode < HttpStatusCode.BAD_REQUEST.getStatusCode()) {
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
            return new ODataResponsePart(response, false);
          }
        }
        requestContext.getCUDRequestHandler().validateChanges(requestContext.getEntityManager());
        t.commit();
        return new ODataResponsePart(responses, true);
      } catch (ODataApplicationException | ODataLibraryException e) {
        // In case of ODataLibraryException the batch request is malformed or the processor implementation is not
        // correct. Throwing an exception will stop the whole batch request not only the Change Set!
        t.rollback();
        throw e;
      } catch (final RollbackException e) {
        if (e.getCause() instanceof OptimisticLockException) {
          throw new ODataJPAProcessorException(e.getCause().getCause(), HttpStatusCode.PRECONDITION_FAILED);
        }
        throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
      }
    } catch (final ODataJPATransactionException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.NOT_IMPLEMENTED);
    }
  }

  /**
   * OData Version 4.0 Part 1: Protocol Plus Errata 02 11.7.2 Batch Request Body states:
   * <p>
   * <cite>
   * The service MUST process the requests within a batch request sequentially. Processing stops on the first error
   * unless the odata.continue-on-error preference is specified. </cite>
   * <p>
   *
   * <i>odata.continue-on-error</i> is explained in OData Version 4.0 Part 1: Protocol Plus Errata 02 8.2.8.3 Preference
   * odata.continue-on-error and states:
   * <p>
   * <cite>
   * The odata.continue-on-error preference on a batch request is used to request that, upon encountering a request
   * within the batch that returns an error, the service return the error for that request and continue processing
   * additional requests within the batch. The syntax of the odata.continue-on-error preference is specified in
   * [OData-ABNF].
   *
   * If not specified, upon encountering an error the service MUST return the error within the batch and stop processing
   * additional requests within the batch.
   *
   * A service MAY specify the support for the odata.continue-on-error preference using an annotation with term
   * Capabilities.BatchContinueOnErrorSupported, see [OData-VocCap]. </cite>
   * <p>
   * So four cases have to be distinguished:
   * <ul>
   * <li>No header</li>
   * <li>Header given without value</li>
   * <li>Header given as true</li>
   * <li>Header given as false</li>
   * </ul>
   * @param preferences
   * @return
   */
  final boolean continueOnError(final Preferences preferences) {
    // Syntax: [ "odata." ] "continue-on-error" [ EQ-h booleanValue ] ; "true" / "false"
    return Optional.ofNullable(preferences.getPreference(PreferenceName.CONTINUE_ON_ERROR.getName()))
        .map(p -> p.getValue() == null ? Boolean.TRUE : Boolean.valueOf(p.getValue()))
        .orElse(false);

  }

  private boolean requestHasFailed(final List<ODataResponse> responses) {
    return responses.get(responses.size() - 1).getStatusCode() >= HttpStatusCode.BAD_REQUEST.getStatusCode();
  }
}
