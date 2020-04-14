package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

import com.sap.olingo.jpa.processor.core.api.JPAODataCRUDContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public interface JPASerializer {

  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException;

  public ContentType getContentType();
  
  default URI buildServiceRoot(final ODataRequest request, final JPAODataCRUDContextAccess serviceContext) throws URISyntaxException {
    if (serviceContext.useAbsoluteContextURL()) {
      final String serviceRoot = request.getRawBaseUri();
      if (serviceRoot == null)
        return null;
      return new URI(serviceRoot.endsWith("/") ? serviceRoot : (serviceRoot + "/"));
    }
    return null;
  }
}