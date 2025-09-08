package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URISyntaxException;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Property;
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

import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

final class JPASerializePrimitiveCollection implements JPAOperationSerializer {
  private final ServiceMetadata serviceMetadata;
  private final ODataSerializer serializer;
  private final ContentType responseFormat;
  private final JPAODataSessionContextAccess serviceContext;

  JPASerializePrimitiveCollection(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final ContentType responseFormat, final JPAODataSessionContextAccess context) {

    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
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
          .asCollection()
          .build();
      final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

      return serializer.primitiveCollection(serviceMetadata, (EdmPrimitiveType) primitiveType, (Property) result,
          options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final JPAEntityCollectionExtension result)
      throws SerializerException, ODataJPASerializerException {
    return null;
  }

}
