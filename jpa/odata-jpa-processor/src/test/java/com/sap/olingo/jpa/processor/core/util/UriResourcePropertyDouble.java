package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceProperty;

public class UriResourcePropertyDouble implements UriResourceProperty {
  private final EdmProperty property;

  public UriResourcePropertyDouble(final EdmProperty property) {
    super();
    this.property = property;
  }

  @Override
  public EdmType getType() {
    return (EdmType) failWithNull();
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public String getSegmentValue(final boolean includeFilters) {
    return (String) failWithNull();
  }

  @Override
  public String toString(final boolean includeFilters) {
    return (String) failWithNull();
  }

  @Override
  public UriResourceKind getKind() {
    return (UriResourceKind) failWithNull();
  }

  @Override
  public String getSegmentValue() {
    return (String) failWithNull();
  }

  @Override
  public EdmProperty getProperty() {

    return property;
  }

  private final Object failWithNull() {
    fail();
    return null;
  }

}
