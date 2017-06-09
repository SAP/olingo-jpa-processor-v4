package com.sap.olingo.jpa.processor.core.serializer;

import static org.mockito.Mockito.mock;

import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.uri.UriInfo;
import org.junit.Before;
import org.junit.Test;

public class TestJPASerializeFunction {
  private JPASerializeFunction cut;
  private ServiceMetadata sm;
  private UriInfo uriInfo;
  private ODataSerializer serializer;

  @Before
  public void setup() {
    sm = mock(ServiceMetadata.class);
    uriInfo = mock(UriInfo.class);
    serializer = mock(ODataSerializer.class);
  }

  @Test
  public void checkCreateForFunctionWithPrimitiveTypeResult() {
    // cut = new JPASerializeFunction(sm, serializer, uriInfo, new JPASerializerFactory(odata, sm));

  }
}
