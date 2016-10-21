package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPASerializeCreate implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final ODataSerializer serializer;

  public JPASerializeCreate(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriInfo uriInfo) throws SerializerException {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public SerializerResult serialize(ODataRequest request, EntityCollection result) throws SerializerException,
      ODataJPASerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

    final EdmEntityType entityType = targetEdmEntitySet.getEntityType();

    final ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .build();

    final EntitySerializerOptions options = EntitySerializerOptions.with()
        .contextURL(contextUrl)
        .build();

    final SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, result
        .getEntities()
        .get(0),
        options);
    return serializerResult;
  }

}
