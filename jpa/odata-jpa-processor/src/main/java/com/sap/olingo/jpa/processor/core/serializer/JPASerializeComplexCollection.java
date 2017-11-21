package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public class JPASerializeComplexCollection implements JPAOperationSerializer {
  private final ServiceMetadata serviceMetadata;
  private final ODataSerializer serializer;
  private final ContentType responseFormat;

  JPASerializeComplexCollection(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final ContentType responseFormat) {

    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.responseFormat = responseFormat;
  }

  @Override
  public SerializerResult serialize(ODataRequest request, EntityCollection result) throws SerializerException,
      ODataJPASerializerException {
    return null;
  }

  @Override
  public SerializerResult serialize(Annotatable result, EdmType complexType) throws SerializerException,
      ODataJPASerializerException {

    final ContextURL contextUrl = ContextURL.with().asCollection().build();
    final ComplexSerializerOptions options = ComplexSerializerOptions.with().contextURL(contextUrl).build();

    return serializer.complexCollection(serviceMetadata, (EdmComplexType) complexType, (Property) result,
        options);
  }

  @Override
  public ContentType getContentType() {
    return responseFormat;
  }

}
