package org.apache.olingo.jpa.processor.core.serializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

class JPASerializeCount implements JPASerializer {

  JPASerializeCount() {

  }

  @Override
  public SerializerResult serialize(ODataRequest request, EntityCollection result) throws SerializerException {
    return new PlainTextCountResult(result);
  }

  private class PlainTextCountResult implements SerializerResult {
    private final EntityCollection result;

    public PlainTextCountResult(final EntityCollection result) {
      this.result = result;
    }

    @Override
    public InputStream getContent() {
      Integer i = result.getCount();
      return new ByteArrayInputStream(i.toString().getBytes());
    }

  }
}
