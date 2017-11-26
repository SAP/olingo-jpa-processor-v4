package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public interface JPASerializer {

  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException;

  public ContentType getContentType();
}
