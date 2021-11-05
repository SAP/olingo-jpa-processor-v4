package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.commons.api.edm.EdmEntityContainer;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.util.matcher.InputStreamMatcher;

class JPAODataServiceDocumentProcessorTest {
  private JPAODataServiceDocumentProcessor cut;
  private JPAODataSessionContextAccess sessionContext;
  private ServiceMetadata metadata;
  private ODataRequest request;
  private ODataResponse response;
  private UriInfo uriInfo;
  private Edm edm;
  private EdmEntityContainer container;

  @BeforeEach
  void setup() {
    sessionContext = mock(JPAODataSessionContextAccess.class);
    edm = mock(Edm.class);
    container = mock(EdmEntityContainer.class);
    metadata = mock(ServiceMetadata.class);
    request = mock(ODataRequest.class);
    response = mock(ODataResponse.class);
    uriInfo = mock(UriInfo.class);
    when(edm.getEntityContainer()).thenReturn(container);
    when(metadata.getEdm()).thenReturn(edm);
  }

  @Test
  void testInstanceCanBeCreated() {
    assertNotNull(new JPAODataServiceDocumentProcessor(sessionContext));
  }

  @Test
  void testInitCanBeCalled() {
    cut = new JPAODataServiceDocumentProcessor(sessionContext);
    cut.init(OData.newInstance(), metadata);
    assertNotNull(cut);
  }

  @Test
  void testCreateServiceDocumentWithRelativeMetadataUrl() throws ODataApplicationException,
      ODataLibraryException {
    when(sessionContext.useAbsoluteContextURL()).thenReturn(false);
    cut = new JPAODataServiceDocumentProcessor(sessionContext);
    cut.init(OData.newInstance(), metadata);
    cut.readServiceDocument(request, response, uriInfo, ContentType.APPLICATION_JSON);
    verify(response).setContent(argThat(new InputStreamMatcher("\"@odata.context\":\"$metadata\"")));
  }

  @Test
  void testCreateServiceDocumentWithAbsoluteMetadataUrl() throws ODataApplicationException,
      ODataLibraryException {
    when(sessionContext.useAbsoluteContextURL()).thenReturn(true);
    when(request.getRawBaseUri()).thenReturn("http://localhost:8080/test");
    cut = new JPAODataServiceDocumentProcessor(sessionContext);
    cut.init(OData.newInstance(), metadata);
    cut.readServiceDocument(request, response, uriInfo, ContentType.APPLICATION_JSON);
    verify(response).setContent(argThat(new InputStreamMatcher(
        "\"@odata.context\":\"http://localhost:8080/test/$metadata\"")));
  }
}
