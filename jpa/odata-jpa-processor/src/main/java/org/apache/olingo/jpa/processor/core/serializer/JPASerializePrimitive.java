package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
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

class JPASerializePrimitive implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final ODataSerializer serializer;

  JPASerializePrimitive(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo) {

    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
//    this.uriHelper = uriHelper;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    final UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo
        .getUriResourceParts().size() - 1);

    Property property = null;
    for (final Property item : result.getEntities().get(0).getProperties()) {
      if (item.getName().equals(uriProperty.getProperty().getName())) {
        property = item;
        break;
      }
    }

    final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getProperty().getType();

    final ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(property.getName())
        .build();

    final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

    final SerializerResult serializerResult = serializer.primitive(serviceMetadata, edmPropertyType, property, options);
    return serializerResult;
  }
}
