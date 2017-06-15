package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;

public class JPASerializeComplexCollection implements JPAFunctionSerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;

  JPASerializeComplexCollection(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo) {

    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
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

}
