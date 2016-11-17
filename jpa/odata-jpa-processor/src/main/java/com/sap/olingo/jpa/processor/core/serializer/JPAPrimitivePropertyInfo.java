package com.sap.olingo.jpa.processor.core.serializer;

import org.apache.olingo.commons.api.data.Property;

class JPAPrimitivePropertyInfo {
  private final String path;
  private final Property property;

  public JPAPrimitivePropertyInfo(final String path, final Property property) {
    super();
    this.path = path;
    this.property = property;
  }

  String getPath() {
    return path;
  }

  Property getProperty() {
    return property;
  }
}
