package com.sap.olingo.jpa.processor.core.serializer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.uri.UriResource;

import com.sap.olingo.jpa.processor.core.util.matcher.ComplexSerializerOptionsMatcher;

public class TestJPASerializerComplexCollection extends TestJPASerializerCollection {

  @SuppressWarnings("unchecked")
  @Override
  protected ComplexSerializerOptionsMatcher createMatcher(final String pattern) {
    return new ComplexSerializerOptionsMatcher(pattern);
  }

  @Override
  protected EdmType getType() {
    return null;
  }

  @Override
  protected void initTest(final List<UriResource> resouceParts) {
    cut = new JPASerializeComplexCollection(serviceMetadata, serializer, ContentType.APPLICATION_JSON, context);
  }

  @Override
  protected <T> void verifySerializerCall(final ODataSerializer serializer, final String pattern)
      throws SerializerException {

    verify(serializer).complexCollection(any(), any(), any(), argThat(createMatcher(pattern)));
  }

}