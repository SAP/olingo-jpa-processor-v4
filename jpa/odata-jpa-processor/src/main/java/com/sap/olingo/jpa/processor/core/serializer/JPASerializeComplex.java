package com.sap.olingo.jpa.processor.core.serializer;

import java.util.List;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;

import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.query.Util;

final class JPASerializeComplex implements JPAOperationSerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfo uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;
  private final ContentType responseFormat;

  JPASerializeComplex(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfo uriInfo, final ContentType responseFormat) {

    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
    this.responseFormat = responseFormat;
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException {

    final EdmEntitySet targetEdmEntitySet = Util.determineTargetEntitySet(uriInfo.getUriResourceParts());
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() - 1);
    final EdmComplexType edmPropertyType = (EdmComplexType) uriProperty.getProperty().getType();

    final String selectList = uriHelper.buildContextURLSelectList(targetEdmEntitySet.getEntityType(),
        uriInfo.getExpandOption(), uriInfo.getSelectOption());

    final ContextURL contextUrl = ContextURL.with()
        .entitySet(targetEdmEntitySet)
        .navOrPropertyPath(Util.determineProptertyNavigationPath(uriInfo.getUriResourceParts()))
        .selectList(selectList)
        .build();
    final ComplexSerializerOptions options = ComplexSerializerOptions.with()
        .contextURL(contextUrl)
        .select(uriInfo.getSelectOption())
        .expand(uriInfo.getExpandOption())
        .build();

    if (uriProperty.getProperty().isCollection()) {
      return serializer.complexCollection(serviceMetadata, edmPropertyType, determineProperty(targetEdmEntitySet,
          result), options);
    } else {
      return serializer.complex(serviceMetadata, edmPropertyType, determineProperty(targetEdmEntitySet, result),
          options);
    }
  }

  @Override
  public SerializerResult serialize(Annotatable result, EdmType complexType) throws SerializerException,
      ODataJPASerializerException {

    final ContextURL contextUrl = ContextURL.with().build();
    final ComplexSerializerOptions options = ComplexSerializerOptions.with().contextURL(contextUrl).build();

    return serializer.complex(serviceMetadata, (EdmComplexType) complexType, (Property) result,
        options);
  }

  @Override
  public ContentType getContentType() {
    return responseFormat;
  }

  private Property determineProperty(final EdmEntitySet targetEdmEntitySet, final EntityCollection result) {
    UriResourceProperty uriProperty = null;
    Property property = null;

    boolean found = false;
    List<Property> properties = result.getEntities().get(0).getProperties();

    for (UriResource hop : uriInfo.getUriResourceParts()) {
      if (hop.getKind().equals(UriResourceKind.entitySet)
          && ((UriResourceEntitySet) hop).getEntitySet() == targetEdmEntitySet
          || hop.getKind().equals(UriResourceKind.navigationProperty)
              && ((UriResourceNavigation) hop).getType() == targetEdmEntitySet.getEntityType())
        found = true;
      if (found && hop.getKind().equals(UriResourceKind.complexProperty)) {
        uriProperty = (UriResourceProperty) hop;
        property = getProperty(uriProperty.getProperty().getName(), properties);
        if (!uriProperty.isCollection() && property != null)// Here it is assumed that the collection is the last hop
                                                            // anyhow
          properties = ((ComplexValue) property.getValue()).getValue();
      }
    }
    return property;
  }

  private Property getProperty(final String name, final List<Property> properties) {
    for (Property p : properties)
      if (p.getName().equals(name) && p.isComplex())
        return p;
    return null;
  }

}
