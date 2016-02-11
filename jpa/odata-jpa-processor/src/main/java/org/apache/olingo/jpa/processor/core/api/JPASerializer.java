package org.apache.olingo.jpa.processor.core.api;

import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;

public interface JPASerializer {

  void init(final OData odata, final ServiceMetadata serviceMetadata);

  public SerializerResult serialize(final ODataRequest request, final ContentType responseFormat,
      final EntityCollection result, final UriInfo uriInfo) throws SerializerException;

}
