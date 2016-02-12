package org.apache.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.jpa.processor.core.api.JPASerializer;
import org.apache.olingo.jpa.processor.core.query.Util;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResourceProperty;

class JPASerializeComplex implements JPASerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;

  JPASerializeComplex(ServiceMetadata serviceMetadata, ODataSerializer serializer, UriHelper uriHelper,
      UriInfo uriInfo) {
    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
  }

  @Override
  public SerializerResult serialize(ODataRequest request, EntityCollection result) throws SerializerException {

    EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());

    Property property = result.getEntities().get(0).getProperties().get(0);

    UriResourceProperty uriProperty = Util.determineStartNavigationPath(uriInfo.getUriResourceParts());
    EdmComplexType edmPropertyType = (EdmComplexType) uriProperty.getProperty().getType();

    String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        uriInfo.getExpandOption(), uriInfo.getSelectOption());

    ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(Util.determineProptertyNavigationPath(uriInfo.getUriResourceParts()))
        .selectList(selectList)
        .build();
    ComplexSerializerOptions options = ComplexSerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    SerializerResult serializerResult = serializer.complex(serviceMetadata, edmPropertyType, property, options);
    return serializerResult;
  }
}
