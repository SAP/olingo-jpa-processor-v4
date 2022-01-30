package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;

public class UriResourceNavigationDouble implements UriResourceNavigation {
  private final EdmType type;
  private final EdmNavigationProperty property;

  public UriResourceNavigationDouble(final EdmEntityType navigationTargetEntity) {
    this(navigationTargetEntity, null);
  }

  public UriResourceNavigationDouble(final EdmType type, final EdmNavigationProperty property) {
    super();
    this.type = type;
    this.property = property;
  }

  @Override
  public EdmType getType() {
    return type;
  }

  @Override
  public boolean isCollection() {
    fail();
    return false;
  }

  @Override
  public String getSegmentValue(final boolean includeFilters) {
    fail();
    return null;
  }

  @Override
  public String toString(final boolean includeFilters) {
    fail();
    return null;
  }

  @Override
  public UriResourceKind getKind() {
    return UriResourceKind.navigationProperty;
  }

  @Override
  public String getSegmentValue() {
    fail();
    return null;
  }

  @Override
  public EdmNavigationProperty getProperty() {
    return property;
  }

  @Override
  public List<UriParameter> getKeyPredicates() {
    fail();
    return null;
  }

  @Override
  public EdmType getTypeFilterOnCollection() {
    return null;
  }

  @Override
  public EdmType getTypeFilterOnEntry() {
    return null;
  }

}
