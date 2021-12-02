package com.sap.olingo.jpa.processor.core.serializer;

import java.util.List;

import org.apache.olingo.commons.api.data.ComplexValue;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceProperty;

public abstract class JPASerializePrimitiveAbstract implements JPAOperationSerializer {
  protected static final char PATH_SEPARATOR = '/';
  protected final ServiceMetadata serviceMetadata;
  protected final UriInfo uriInfo;

  protected JPASerializePrimitiveAbstract(final ServiceMetadata serviceMetadata, final UriInfo uriInfo) {
    super();
    this.serviceMetadata = serviceMetadata;
    this.uriInfo = uriInfo;
  }

  protected final JPAPrimitivePropertyInfo determinePrimitiveProperty(final EntityCollection result,
      final List<UriResource> uriResources) {
    Property property = null;
    Object value = null;

    final StringBuilder path = new StringBuilder();

    for (final Property item : result.getEntities().get(0).getProperties())
      if (partOfPath(item, uriResources)) {
        property = item;
        boolean found = false;
        while (!found) {
          path.append(property.getName());
          if (property.getValue() instanceof ComplexValue) {
            value = property.getValue();
            property = ((ComplexValue) value).getValue().get(0);
            path.append(PATH_SEPARATOR);
          } else {
            found = true;
          }
        }
        break;
      }
    return new JPAPrimitivePropertyInfo(path.toString(), property);
  }

  private boolean partOfPath(final Property item, final List<UriResource> uriResources) {
    for (final UriResource resource : uriResources)
      if (resource instanceof UriResourceProperty
          && ((UriResourceProperty) resource).getProperty().getName().equals(item.getName()))
        return true;
    return false;
  }

}