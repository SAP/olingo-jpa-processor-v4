package com.sap.olingo.jpa.processor.core.serializer;

import java.io.InputStream;
import java.util.List;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.FixedFormatSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveValueSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

final class JPASerializeValue extends JPASerializePrimitiveAbstract {
  private final FixedFormatSerializer serializer;

  JPASerializeValue(final ServiceMetadata serviceMetadata, final FixedFormatSerializer serializer,
      final UriInfo uriInfo) {

    super(serviceMetadata, uriInfo);
    this.serializer = serializer;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {

    if (result.getEntities().get(0) == null
        || result.getEntities().get(0).getProperties() == null
        || result.getEntities().get(0).getProperties().isEmpty()) {
      throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.RESULT_NOT_FOUND,
          HttpStatusCode.INTERNAL_SERVER_ERROR);
    }

    InputStream serializerResult = null;
    if (isStream()) {
      final Entity et = result.getEntities().get(0);
      final EdmEntityType edmEt = serviceMetadata.getEdm().getEntityType(new FullQualifiedName(et.getType()));
      final List<EdmKeyPropertyRef> p = edmEt.getKeyPropertyRefs();
      Property property = null;
      for (final Property item : result.getEntities().get(0).getProperties()) {
        if (!isKey(p, item)) {
          property = item;
          break;
        }
      }
      if (property == null)
        throw new ODataJPASerializerException(ODataJPASerializerException.MessageKeys.RESULT_NOT_FOUND,
            HttpStatusCode.INTERNAL_SERVER_ERROR);
      serializerResult = serializer.binary((byte[]) property.getValue());
    } else {

      final UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo
          .getUriResourceParts().size() - 2);

      final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getType();

      final JPAPrimitivePropertyInfo info = determinePrimitiveProperty(result, uriInfo.getUriResourceParts());
      final PrimitiveValueSerializerOptions options = PrimitiveValueSerializerOptions.with().build();
      if (!info.getProperty().isNull())
        serializerResult = serializer.primitiveValue(edmPropertyType, info.getProperty().getValue(), options);
    }
    return new JPAValueSerializerResult(serializerResult);
  }

  @Override
  public SerializerResult serialize(final Annotatable result, final EdmType entityType)
      throws SerializerException, ODataJPASerializerException {

    final PrimitiveValueSerializerOptions options = PrimitiveValueSerializerOptions.with().build();
    return new JPAValueSerializerResult(serializer.primitiveValue((EdmPrimitiveType) entityType, ((Property) result)
        .getValue(), options));

  }

  private boolean isStream() {
    final UriResource successor = uriInfo.getUriResourceParts().get(uriInfo.getUriResourceParts().size() - 2);

    return (successor instanceof UriResourceEntitySet
        || successor instanceof UriResourceNavigation && ((UriResourceNavigation) successor)
            .getType() instanceof EdmEntityType);
  }

  private boolean isKey(final List<EdmKeyPropertyRef> keyist, final Property item) {
    for (final EdmKeyPropertyRef key : keyist) {
      if (key.getName().equals(item.getName()))
        return true;
    }
    return false;
  }

  @Override
  public ContentType getContentType() {
    return ContentType.TEXT_PLAIN;
  }
}
