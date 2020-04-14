package com.sap.olingo.jpa.processor.core.util.matcher;

import java.net.URI;

import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;

public class ComplexSerializerOptionsMatcher extends SerializerOptionsMatcher<ComplexSerializerOptions> {

  public ComplexSerializerOptionsMatcher(final String pattern) {
    super(pattern);
  }

  @Override
  protected URI getService(final ComplexSerializerOptions options) {
    return options.getContextURL().getServiceRoot();
  }
}
