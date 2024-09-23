package com.sap.olingo.jpa.processor.core.serializer;

import java.net.URISyntaxException;
import java.util.List;

import org.apache.olingo.commons.api.data.Annotatable;
import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.edm.EdmBindingTarget;
import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.serializer.ComplexSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerException;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriHelper;
import org.apache.olingo.server.api.uri.UriInfoResource;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.UriResourceProperty;
import org.apache.olingo.server.api.uri.UriResourceSingleton;

import com.sap.olingo.jpa.processor.core.api.JPAODataSessionContextAccess;
import com.sap.olingo.jpa.processor.core.exception.ODataJPASerializerException;
import com.sap.olingo.jpa.processor.core.query.Utility;

public final class JPASerializeComplex implements JPAOperationSerializer {
  private final ServiceMetadata serviceMetadata;
  private final UriInfoResource uriInfo;
  private final UriHelper uriHelper;
  private final ODataSerializer serializer;
  private final ContentType responseFormat;
  private final JPAODataSessionContextAccess serviceContext;

  JPASerializeComplex(final ServiceMetadata serviceMetadata, final ODataSerializer serializer,
      final UriHelper uriHelper, final UriInfoResource uriInfo, final ContentType responseFormat,
      final JPAODataSessionContextAccess serviceContext) {

    this.uriInfo = uriInfo;
    this.serializer = serializer;
    this.serviceMetadata = serviceMetadata;
    this.uriHelper = uriHelper;
    this.responseFormat = responseFormat;
    this.serviceContext = serviceContext;
  }

  @Override
  public ContentType getContentType() {
    return responseFormat;
  }

  @Override
  public SerializerResult serialize(final Annotatable result, final EdmType complexType, final ODataRequest request)
      throws SerializerException, ODataJPASerializerException {

    try {
      final ContextURL contextUrl = ContextURL.with().serviceRoot(buildServiceRoot(request, serviceContext)).build();
      final ComplexSerializerOptions options = ComplexSerializerOptions.with().contextURL(contextUrl).build();
      return serializer.complex(serviceMetadata, (EdmComplexType) complexType, (Property) result, options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  @Override
  public SerializerResult serialize(final ODataRequest request, final EntityCollection result)
      throws SerializerException, ODataJPASerializerException {

    final EdmBindingTarget targetEdmBindingTarget = Utility.determineBindingTarget(uriInfo.getUriResourceParts());
    final List<UriResource> resourceParts = uriInfo.getUriResourceParts();
    final UriResourceProperty uriProperty = (UriResourceProperty) resourceParts.get(resourceParts.size() - 1);
    final EdmComplexType edmPropertyType = ((UriResourceComplexProperty) uriProperty).getComplexType();

    final String selectList = uriHelper.buildContextURLSelectList(targetEdmBindingTarget.getEntityType(),
        uriInfo.getExpandOption(), uriInfo.getSelectOption());

    try {
      final ContextURL contextUrl = ContextURL.with()
          .serviceRoot(buildServiceRoot(request, serviceContext))
          .entitySetOrSingletonOrType(targetEdmBindingTarget.getName())
          .navOrPropertyPath(Utility.determinePropertyNavigationPath(uriInfo.getUriResourceParts()))
          .selectList(selectList)
          .build();

      final ComplexSerializerOptions options = ComplexSerializerOptions.with()
          .contextURL(contextUrl)
          .select(uriInfo.getSelectOption())
          .expand(uriInfo.getExpandOption())
          .build();

      if (uriProperty.getProperty().isCollection())
        return serializer.complexCollection(serviceMetadata, edmPropertyType, determineProperty(targetEdmBindingTarget,
            result), options);
      else
        return serializer.complex(serviceMetadata, edmPropertyType, determineProperty(targetEdmBindingTarget, result),
            options);
    } catch (final URISyntaxException e) {
      throw new ODataJPASerializerException(e, HttpStatusCode.BAD_REQUEST);
    }
  }

  private Property determineProperty(final EdmBindingTarget targetEdmBindingTarget, final EntityCollection result) {
    UriResourceProperty uriProperty = null;
    Property property = null;

    boolean found = false;
    List<Property> properties = result.getEntities().get(0).getProperties();

    for (final UriResource hop : uriInfo.getUriResourceParts()) {
      if (isTopLevel(hop)
          && asTopLevel(hop) == targetEdmBindingTarget
          || hop.getKind().equals(UriResourceKind.navigationProperty)
              && ((UriResourceNavigation) hop).getType() == targetEdmBindingTarget.getEntityType())
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

  protected EdmBindingTarget asTopLevel(final UriResource hop) {
    return hop.getKind().equals(UriResourceKind.entitySet)
        ? ((UriResourceEntitySet) hop).getEntitySet()
        : ((UriResourceSingleton) hop).getSingleton();
  }

  protected boolean isTopLevel(final UriResource hop) {
    return hop.getKind().equals(UriResourceKind.entitySet)
        || hop.getKind().equals(UriResourceKind.singleton);
  }

  private Property getProperty(final String name, final List<Property> properties) {
    for (final Property p : properties)
      if (p.getName().equals(name) && p.isComplex())
        return p;
    return null;
  }

}
