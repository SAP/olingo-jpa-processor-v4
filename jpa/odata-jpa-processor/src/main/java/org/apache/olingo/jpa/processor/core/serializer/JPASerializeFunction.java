package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceFunction;

public class JPASerializeFunction implements JPASerializer {
  private ServiceMetadata serviceMetadata;
  private OData odata;

  @Override
  public final void init(final OData odata, final ServiceMetadata serviceMetadata) {
    this.odata = odata;
    this.serviceMetadata = serviceMetadata;
  }

  @Override
  public SerializerResult serialize(ODataRequest request, ContentType responseFormat,
      EntityCollection result, UriInfo uriInfo) throws SerializerException {

    UriResourceFunction uriResource = (UriResourceFunction) uriInfo.getUriResourceParts().get(0);
    EdmEntityType edmEntityType = (EdmEntityType) uriResource.getFunction().getReturnType().getType();

    ContextURL contextURL = ContextURL.with()
        .type(edmEntityType)
        .build();

    ODataSerializer serializer = odata.createSerializer(responseFormat);
    if (uriResource.isCollection()) {
      EntityCollectionSerializerOptions options = EntityCollectionSerializerOptions.with()
          .contextURL(contextURL)
          .build();
      return serializer.entityCollection(serviceMetadata, edmEntityType, result, options);
    } else {
      EntitySerializerOptions options = EntitySerializerOptions.with()
          .contextURL(contextURL)
          .build();
      return serializer.entity(serviceMetadata, edmEntityType, result.getEntities().get(0), options);
    }
  }
}
