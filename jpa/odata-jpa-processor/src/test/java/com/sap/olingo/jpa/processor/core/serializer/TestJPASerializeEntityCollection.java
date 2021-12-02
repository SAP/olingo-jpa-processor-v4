package com.sap.olingo.jpa.processor.core.serializer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriResource;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.util.matcher.EntityCollectionSerializerOptionsMatcher;

public class TestJPASerializeEntityCollection extends TestJPAOperationSerializer {

  @Override
  @Test
  public void testRequestContextUrlFilledForAbsoluteRequestedWithOutSlash() throws SerializerException,
      ODataJPASerializerException {
    when(context.useAbsoluteContextURL()).thenReturn(true);
    when(request.getRawBaseUri()).thenReturn("localhost:8080/v1");
    cut.serialize(request, result);
    verify(serializer).entityCollection(any(), any(), any(), argThat(new EntityCollectionSerializerOptionsMatcher(
        "localhost:8080/v1/")));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected EntityCollectionSerializerOptionsMatcher createMatcher(final String pattern) {
    return new EntityCollectionSerializerOptionsMatcher(pattern);
  }

  @Override
  protected EdmType getType() {
    return edmEt;
  }

  @Override
  protected void initTest(final List<UriResource> resourceParts) {
    annotatable = mock(EntityCollection.class);
    cut = new JPASerializeEntityCollection(serviceMetadata, serializer, uriHelper, uriInfo,
        ContentType.APPLICATION_JSON, context);
  }

  @Override
  protected <T> void verifySerializerCall(final ODataSerializer serializer, final String pattern)
      throws SerializerException {
    
    verify(serializer).entityCollection(any(), any(), any(), argThat(createMatcher(pattern)));
  }
}