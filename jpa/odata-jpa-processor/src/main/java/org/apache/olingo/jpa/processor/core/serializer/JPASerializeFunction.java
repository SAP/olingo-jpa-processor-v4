package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceFunction;

class JPASerializeFunction implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;

  JPASerializeFunction(ServiceMetadata serviceMetadata, ODataSerializer serializer, UriHelper uriHelper,
      UriInfo uriInfo) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
  }

  @Override
  public SerializerResult serialize(ODataRequest request, EntityCollection result) throws SerializerException {

    UriResourceFunction uriResource = (UriResourceFunction) uriInfo.getUriResourceParts().get(0);
    EdmEntityType edmEntityType = (EdmEntityType) uriResource.getFunction().getReturnType().getType();

    ContextURL contextURL = ContextURL.with()
        .type(edmEntityType)
        .build();

    if (uriResource.isCollection()) {
      EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
          .contextURL(contextURL)
          .build();
      return serializer.entityCollection(serviceMetadata, edmEntityType, result, options);
    } else {
      EntitySerializerOptions options = EntitySerializerOptions.with()
          .contextURL(contextURL)
          .build();
      return serializer.entity(serviceMetadata, edmEntityType, result.getEntities().get(0), options);
    }
  }
}
