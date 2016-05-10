package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceProperty;

class JPASerializePrimitive extends JPASerializePrimitiveAbstract {
  private final ODataSerializer serializer;

  JPASerializePrimitive(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo) {

    super(serviceMetadata, uriHelper, uriInfo);
    this.serializer = serializer;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    final UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo
        .getUriResourceParts().size() - 1);

    final JPAPrimitivePropertyInfo property = determinePrimitiveProperty(result, uriInfo.getUriResourceParts());
    final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getProperty().getType();

    final ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(property.getPath())
        .build();

    final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

    final SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property
        .getProperty(), options);
    return serializerResult;
  }
}
