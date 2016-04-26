package org.apache.olingo.jpa.processor.core.serializer;

import java.io.InputStream;

import org.apache.olingo.server.api.serializer.SerializerResult;

class JPAValueSerializerResult implements SerializerResult {
  /**
   * 
   */
  private final InputStream result;

  public JPAValueSerializerResult(final InputStream inputStream) {
    this.result = inputStream;
  }

  @Override
  public InputStream getContent() {
    return result;
  }
}