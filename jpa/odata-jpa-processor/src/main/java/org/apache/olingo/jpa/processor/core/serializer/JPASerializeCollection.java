package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

class JPASerializeCollection implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;

  JPASerializeCollection(ServiceMetadata serviceMetadata, ODataSerializer serializer, UriHelper uriHelper,
      UriInfo uriInfo) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

    String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        null, uriInfo.getSelectOption());

    ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .selectList(selectList)
        .build();

    final String id = request.getRawBaseUri() + "/" + targetEdmEntitySet.getEntityType().getName();
    EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with()
        .contextURL(contextUrl)
        .id(id)
        .count(uriInfo.getCountOption())
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    SerializerResult serializerResult = serializer.entityCollection(this.serviceMetadata, targetEdmEntitySet
        .getEntityType(), result, opts);
    return serializerResult;

  }

}
