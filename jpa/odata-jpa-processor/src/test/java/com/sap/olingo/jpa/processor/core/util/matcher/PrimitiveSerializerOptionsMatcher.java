package com.sap.olingo.jpa.processor.core.util.matcher;

import java.net.URI;

import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;

public class PrimitiveSerializerOptionsMatcher extends SerializerOptionsMatcher<PrimitiveSerializerOptions> {

  public PrimitiveSerializerOptionsMatcher(final String pattern) {
    super(pattern);
  }

  @Override
  protected URI getService(final PrimitiveSerializerOptions options) {
    return options.getContextURL().getServiceRoot();
  }
}
