package com.sap.olingo.jpa.processor.core.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataHandler;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.deserializer.batch.BatchRequestPart;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Answers;

import com.sap.olingo.jpa.processor.core.api.JPAODataRequestContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger;
import com.sap.olingo.jpa.processor.core.api.JPAServiceDebugger.JPARuntimeMeasurment;

abstract class JPAODataBatchAbstractRequestGroupTest {

  protected JPAODataParallelBatchProcessor processor;
  protected ODataHandler odataHandler;
  private JPAODataRequestContextAccess requestContext;
  private JPAODataSessionContextAccess serviceContext;
  private OData odata;
  private ServiceMetadata serviceMetadata;
  protected List<BatchRequestPart> groupElements;
  private JPAServiceDebugger debugger;
  private JPARuntimeMeasurment measurment;

  @BeforeEach
  void setup() throws ODataApplicationException, ODataLibraryException {

    debugger = mock(JPAServiceDebugger.class);
    odata = mock(OData.class);
    serviceMetadata = mock(ServiceMetadata.class);
    serviceContext = mock(JPAODataSessionContextAccess.class);
    requestContext = mock(JPAODataRequestContextAccess.class, withSettings().defaultAnswer(Answers.RETURNS_DEEP_STUBS));
    odataHandler = mock(ODataHandler.class);
    measurment = mock(JPARuntimeMeasurment.class);
    processor = spy(new JPAODataParallelBatchProcessor(serviceContext, requestContext));
    groupElements = new ArrayList<>();

    processor.init(odata, serviceMetadata);

    when(odata.createRawHandler(serviceMetadata)).thenReturn(odataHandler);
    when(requestContext.getDebugger()).thenReturn(debugger);
    when(debugger.newMeasurement(any(), any())).thenReturn(measurment);

  }

  protected ODataRequest buildPart() {
    final BatchRequestPart part = mock(BatchRequestPart.class);
    final List<ODataRequest> get = new ArrayList<>(1);
    final ODataRequest request = mock(ODataRequest.class);
    get.add(request);
    groupElements.add(part);
    when(part.getRequests()).thenReturn(get);
    return request;
  }

}