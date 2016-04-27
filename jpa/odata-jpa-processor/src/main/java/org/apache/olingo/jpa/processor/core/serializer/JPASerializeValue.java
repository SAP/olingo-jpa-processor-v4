package org.apache.olingo.jpa.processor.core.serializer;

import java.io.InputStream;
import java.util.List;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveValueSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceProperty;

class JPASerializeValue extends JPASerializePrimitiveAbstract implements JPASerializer {
  private final FixedFormatSerializer serializer;

  JPASerializeValue(final ServiceMetadata serviceMetadata, final FixedFormatSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo) {

    super(serviceMetadata, uriHelper, uriInfo);
    this.serializer = serializer;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    Property property = null;
    InputStream serializerResult = null;
    Entity et = result.getEntities().get(0);

    if (isStream()) {
      EdmEntityType edmEt = serviceMetadata.getEdm().getEntityType(new FullQualifiedName(et.getType()));
      List<EdmKeyPropertyRef> p = edmEt.getKeyPropertyRefs();
      for (final Property item : result.getEntities().get(0).getProperties()) {
        if (!isKey(p, item)) {
          property = item;
          break;
        }
      }
      serializerResult = serializer.binary((byte[]) property.getValue());
    } else {

      final UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo
          .getUriResourceParts().size() - 2);

      final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getProperty().getType();

      JPAPrimitivePropertyInfo info = determinePrimitiveProperty(result, uriInfo.getUriResourceParts());
      final PrimitiveValueSerializerOptions options = PrimitiveValueSerializerOptions.with().build();
      serializerResult = serializer.primitiveValue(edmPropertyType, info.getProperty().getValue(), options);
    }
    return new JPAValueSerializerResult(serializerResult);
  }

  private boolean isStream() {

    return uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 2) instanceof UriResourceEntitySet
        ? true : false;
  }

  private boolean isKey(List<EdmKeyPropertyRef> keyist, Property item) {
    for (EdmKeyPropertyRef key : keyist) {
      if (key.getName().equals(item.getName()))
        return true;
    }
    return false;
  }
}
