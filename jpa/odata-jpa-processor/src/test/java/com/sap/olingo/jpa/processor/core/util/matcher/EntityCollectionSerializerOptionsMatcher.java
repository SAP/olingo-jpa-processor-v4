package com.sap.olingo.jpa.processor.core.util.matcher;

import java.net.URI;

import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;

public class EntityCollectionSerializerOptionsMatcher extends
    SerializerOptionsMatcher<EntityCollectionSerializerOptions> {

  public EntityCollectionSerializerOptionsMatcher(final String pattern) {
    super(pattern);
  }

  @Override
  protected URI getService(final EntityCollectionSerializerOptions options) {
    return options.getContextURL().getServiceRoot();
  }
}
