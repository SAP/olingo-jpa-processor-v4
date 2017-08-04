package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.PrimitiveSerializerOptions;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

final class JPASerializePrimitiveCollection implements JPAFunctionSerializer {
  private final ServiceMetadata serviceMetadata;
  private final ODataSerializer serializer;

  JPASerializePrimitiveCollection(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo) throws SerializerException {

    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public SerializerResult serialize(Annotatable result, EdmType primitiveType) throws SerializerException,
      ODataJPASerializerException {
    final ContextURL contextUrl = ContextURL.with().asCollection().build();
    final PrimitiveSerializerOptions options = PrimitiveSerializerOptions.with().contextURL(contextUrl).build();

    return serializer.primitiveCollection(serviceMetadata, (EdmPrimitiveType) primitiveType, (Property) result,
        options);
  }

  @Override
  public SerializerResult serialize(ODataRequest request, EntityCollection result) throws SerializerException,
      ODataJPASerializerException {
    return null;
  }

}
