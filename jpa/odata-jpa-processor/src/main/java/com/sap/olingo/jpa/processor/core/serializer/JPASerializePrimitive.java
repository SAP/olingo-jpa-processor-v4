package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.query.Util;

final class JPASerializePrimitive extends JPASerializePrimitiveAbstract {
  private final ODataSerializer serializer;
  private final ContentType responseFormat;
  private final JPAODataSessionContextAccess serviceContext;

  JPASerializePrimitive(final ServiceMetadata serviceMetadata, final ODataSerializer serializer, final UriInfo uriInfo,
      final ContentType responseFormat, final JPAODataSessionContextAccess context) {

    super(serviceMetadata, uriInfo);
    this.serializer = serializer;
    this.responseFormat = responseFormat;
    this.serviceContext = context;
  }

  @Override
  public ContentType getContentType() {
    return responseFormat;
  }

  @Override
  public SerializerResult serialize(final Annotatable result, final EdmType primitiveType, final ODataRequest request)
      throws SerializerException, ODataJPASerializerException {

    try {
      final ContextURL contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .build();
      final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

      return serializer.primitive(serviceMetadata, (EdmPrimitiveType) primitiveType, (Property) result,
          options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    final UriResourceProperty uriProperty = (UriResourceProperty) uriInfo.getUriResourceParts().get(uriInfo
        .getUriResourceParts().size() - 1);

    final JPAPrimitivePropertyInfo property = determinePrimitiveProperty(result, uriInfo.getUriResourceParts());
    final EdmPrimitiveType edmPropertyType = (EdmPrimitiveType) uriProperty.getProperty().getType();

    try {
      final ContextURL contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .entitySet(targetEdmEntitySet)
          .navOrPropertyPath(property.getPath())
          .build();

      final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();
      if (uriProperty.getProperty().isCollection())
        return serializer.primitiveCollection(serviceMetadata, edmPropertyType, property.getProperty(), options);
      else
        return serializer.primitive(serviceMetadata, edmPropertyType, property.getProperty(), options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }
}
