package com.sap.olingo.jpa.processor.core.util.matcher;

import java.net.URI;

import org.apache.olingo.server.api.serializer.EntitySerializerOptions;

public class EntitySerializerOptionsMatcher extends SerializerOptionsMatcher<EntitySerializerOptions> {

  public EntitySerializerOptionsMatcher(final String pattern) {
    super(pattern);
  }

  @Override
  protected URI getService(final EntitySerializerOptions options) {
    return options.getContextURL().getServiceRoot();
  }
}
