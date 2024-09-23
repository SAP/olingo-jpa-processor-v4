package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.commons.api.edm.EdmComplexType;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriResourceComplexProperty;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.core.uri.UriResourceTypedImpl;

class JPAUriResourceComplexProperty extends UriResourceTypedImpl implements UriResourceComplexProperty {

  private final EdmProperty property;

  public JPAUriResourceComplexProperty(final EdmProperty property) {
    super(UriResourceKind.complexProperty);
    this.property = property;
  }

  @Override
  public EdmProperty getProperty() {
    return property;
  }

  @Override
  public EdmComplexType getComplexType() {
    return (EdmComplexType) getType();
  }

  @Override
  public EdmComplexType getComplexTypeFilter() {
    return (EdmComplexType) getTypeFilter();
  }

  @Override
  public EdmType getType() {
    return property.getType();
  }

  @Override
  public boolean isCollection() {
    return property.isCollection();
  }

  @Override
  public String getSegmentValue() {
    return property.getName();
  }
}
