package com.sap.olingo.jpa.processor.core.uri;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.UriResourceNavigation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

public class JPAUriResourceNavigationImpl implements UriResourceNavigation {

  private final EdmNavigationProperty navigationProperty;
  private final List<UriParameter> keyPredicates;

  public JPAUriResourceNavigationImpl(@Nonnull final EdmNavigationProperty edmNavigationProperty) {
    this.navigationProperty = Objects.requireNonNull(edmNavigationProperty, "EdmNavigationProperty not provided");
    keyPredicates = Collections.emptyList();

  }

  public JPAUriResourceNavigationImpl(@Nonnull final EdmNavigationProperty edmNavigationProperty,
      @Nonnull final String keyPath) {
    this.navigationProperty = Objects.requireNonNull(edmNavigationProperty, "EdmNavigationProperty not provided");

    final var keys = edmNavigationProperty.getType().getKeyPropertyRefs();
    final var keyValues = Objects.requireNonNull(keyPath).split(JPAPath.PATH_SEPARATOR);
    this.keyPredicates = new ArrayList<>(keys.size());

    for (int i = 0; i < keys.size(); i++) {
      keyPredicates.add(new JPAUriParameterImpl(keys.get(i), keyValues[i]));
    }
  }

  @Override
  public String getSegmentValue(final boolean includeFilters) {
    return null;
  }

  @Override
  public String toString(final boolean includeFilters) {
    return toString();
  }

  @Override
  public UriResourceKind getKind() {
    return UriResourceKind.navigationProperty;
  }

  @Override
  public EdmNavigationProperty getProperty() {
    return navigationProperty;
  }

  @Override
  public EdmType getType() {
    return navigationProperty.getType();
  }

  @Override
  public boolean isCollection() {
    return navigationProperty.isCollection() && keyPredicates.isEmpty();
  }

  @Override
  public String getSegmentValue() {
    return navigationProperty.getName();
  }

  @Override
  public EdmType getTypeFilterOnCollection() {
    return null;
  }

  @Override
  public EdmType getTypeFilterOnEntry() {
    return null;
  }

  @Override
  public List<UriParameter> getKeyPredicates() {
    return keyPredicates;
  }

  @Override
  public String toString() {
    return getSegmentValue();
  }
}
