package org.apache.olingo.jpa.processor.core.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAProcessorException;
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

/**
 * 
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/os/part1-protocol/odata-v4.0-os-part1-protocol.html#_Toc372793748"> aa
 * </a>
 * 
 * @author Oliver Grande
 *
 */
public class JPAODataBatchProcessor implements BatchProcessor {

//  private final JPAODataContextAccess context;
//  private final EntityManagerFactory emf;
  private OData odata;

  public JPAODataBatchProcessor() {
//    this.context = context;
//    this.emf = emf;
  }

  @Override
  public void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
  }

  @Override
  public void processBatch(final BatchFacade facade, final ODataRequest request, final ODataResponse response)
      throws ODataApplicationException, ODataLibraryException {
    final String boundary = facade.extractBoundaryFromContentType(request.getHeader(HttpHeader.CONTENT_TYPE));
    final BatchOptions options = BatchOptions.with()
        .rawBaseUri(request.getRawBaseUri())
        .rawServiceResolutionUri(request.getRawServiceResolutionUri())
        .build();
    final List<BatchRequestPart> requestParts = odata.createFixedFormatDeserializer()
        .parseBatchRequest(request.getBody(), boundary, options);

    final List<ODataResponsePart> responseParts = new ArrayList<ODataResponsePart>();
    for (final BatchRequestPart part : requestParts) {
      responseParts.add(facade.handleBatchRequest(part));
    }
    final InputStream responseContent = odata.createFixedFormatSerializer().batchResponse(responseParts, boundary);
    final String responseBoundary = "batch_" + UUID.randomUUID().toString();

    response.setHeader(HttpHeader.CONTENT_TYPE, ContentType.MULTIPART_MIXED + ";boundary=" + responseBoundary);
    response.setContent(responseContent);
    response.setStatusCode(HttpStatusCode.ACCEPTED.getStatusCode());
  }

  @Override
  public ODataResponsePart processChangeSet(final BatchFacade facade, final List<ODataRequest> requests)
      throws ODataApplicationException, ODataLibraryException {
    // TODO handle modifying requests
    throw new ODataJPAProcessorException(ODataJPAProcessorException.MessageKeys.BATCH_CHANGE_SET_NOT_IMPLEMENTED,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

}
