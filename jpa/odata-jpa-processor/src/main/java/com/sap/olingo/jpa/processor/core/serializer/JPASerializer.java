package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public interface JPASerializer {

  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException;

  public ContentType getContentType();

  default URI buildServiceRoot(final ODataRequest request, final JPAODataSessionContextAccess serviceContext)
      throws URISyntaxException {

    final String pathSeperator = "/";
    if (serviceContext.useAbsoluteContextURL()) {
      final String serviceRoot = request.getRawBaseUri();
      if (serviceRoot == null)
        return null;
      return new URI(serviceRoot.endsWith(pathSeperator) ? serviceRoot : (serviceRoot + pathSeperator));
    }
    return null;
  }
}