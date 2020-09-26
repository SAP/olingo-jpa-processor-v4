package com.sap.olingo.jpa.processor.core.api;

import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.etag.ETagHelper;
import org.apache.olingo.server.api.etag.ServiceMetadataETagSupport;
import org.apache.olingo.server.api.processor.DefaultProcessor;
import org.apache.olingo.server.api.processor.ServiceDocumentProcessor;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPAODataServiceDocumentProcessor implements ServiceDocumentProcessor {

  private OData odata;
  private ServiceMetadata serviceMetadata;
  private final JPAODataCRUDContextAccess serviceContext;

  public JPAODataServiceDocumentProcessor(final JPAODataCRUDContextAccess serviceContext) {
    super();
    this.serviceContext = serviceContext;
  }

  @Override
  public void init(OData odata, ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  /**
   * This is a copy from @see
   * {@link DefaultProcessor#readServiceDocument(ODataRequest, ODataResponse, UriInfo, ContentType)}
   * 
   */
  @Override
  public void readServiceDocument(final ODataRequest request, final ODataResponse response, final UriInfo uriInfo,
      final ContentType requestedContentType) throws ODataApplicationException, ODataLibraryException {
    String uri = serviceContext.useAbsoluteContextURL() ? request.getRawBaseUri() : null;
    boolean isNotModified = false;
    ServiceMetadataETagSupport eTagSupport = serviceMetadata.getServiceMetadataETagSupport();
    if (eTagSupport != null && eTagSupport.getServiceDocumentETag() != null) {
      // Set application etag at response
      response.setHeader(HttpHeader.ETAG, eTagSupport.getServiceDocumentETag());
      // Check if service document has been modified
      ETagHelper eTagHelper = odata.createETagHelper();
      isNotModified = eTagHelper.checkReadPreconditions(eTagSupport.getServiceDocumentETag(), request
          .getHeaders(HttpHeader.IF_MATCH), request.getHeaders(HttpHeader.IF_NONE_MATCH));
    }

    // Send the correct response
    if (isNotModified) {
      response.setStatusCode(HttpStatusCode.NOT_MODIFIED.getStatusCode());
    } else {
      // HTTP HEAD requires no payload but a 200 OK response
      if (HttpMethod.HEAD == request.getMethod()) {
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
      } else {
        ODataSerializer serializer = odata.createSerializer(requestedContentType);
        response.setContent(serializer.serviceDocument(serviceMetadata, uri).getContent());
        response.setStatusCode(HttpStatusCode.OK.getStatusCode());
        response.setHeader(HttpHeader.CONTENT_TYPE, requestedContentType.toContentTypeString());
      }
    }
  }
}

