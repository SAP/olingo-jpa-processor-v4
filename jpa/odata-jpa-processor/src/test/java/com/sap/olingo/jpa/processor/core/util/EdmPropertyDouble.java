package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmMapping;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.geo.SRID;

public class EdmPropertyDouble implements EdmProperty {
  private final String name;

  public EdmPropertyDouble(final String name) {
    super();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public EdmType getType() {
    return (EdmType) failWithNull();
  }

  @Override
  public boolean isCollection() {
    return (boolean) failWithNull();
  }

  @Override
  public EdmMapping getMapping() {
    return (EdmMapping) failWithNull();
  }

  @Override
  public EdmAnnotation getAnnotation(final EdmTerm term, final String qualifier) {
    return (EdmAnnotation) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<EdmAnnotation> getAnnotations() {
    return (List<EdmAnnotation>) failWithNull();
  }

  @Override
  public String getMimeType() {
    return (String) failWithNull();
  }

  @Override
  public boolean isPrimitive() {
    return (boolean) failWithNull();
  }

  @Override
  public boolean isNullable() {
    return (boolean) failWithNull();
  }

  @Override
  public Integer getMaxLength() {
    return (Integer) failWithNull();
  }

  @Override
  public Integer getPrecision() {
    return (Integer) failWithNull();
  }

  @Override
  public Integer getScale() {
    return (Integer) failWithNull();
  }

  @Override
  public SRID getSrid() {
    return (SRID) failWithNull();
  }

  @Override
  public boolean isUnicode() {
    return (boolean) failWithNull();
  }

  @Override
  public String getDefaultValue() {
    return (String) failWithNull();
  }

  @Override
  public EdmType getTypeWithAnnotations() {
    return (EdmType) failWithNull();
  }

  @Override
  public String getScaleAsString() {
    return (String) failWithNull();
  }

  private final Object failWithNull() {
    fail();
    return null;
  }
}
