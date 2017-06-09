package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public interface JPAFunctionSerializer extends JPASerializer {
  public SerializerResult serialize(final ODataRequest request, final Object result)
      throws SerializerException, ODataJPASerializerException;
}
