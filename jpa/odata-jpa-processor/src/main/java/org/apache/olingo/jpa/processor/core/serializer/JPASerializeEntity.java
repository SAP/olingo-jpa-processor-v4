package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

public class JPASerializeEntity implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;

  public JPASerializeEntity(ServiceMetadata serviceMetadata, ODataSerializer serializer, UriHelper uriHelper,
      UriInfo uriInfo) throws SerializerException {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

    EdmEntityType entityType = targetEdmEntitySet.getEntityType();

    String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        uriInfo.getExpandOption(), uriInfo.getSelectOption());

    ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet).suffix(Suffix.ENTITY)
        .selectList(selectList)
        .build();

    EntitySerializerOptions options = EntitySerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    SerializerResult serializerResult = serializer.entity(serviceMetadata, entityType, result
        .getEntities()
        .get(0),
        options);
    return serializerResult;
  }
}
