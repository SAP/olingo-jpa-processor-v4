package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmOnDelete;
import org.apache.olingo.commons.api.edm.EdmReferentialConstraint;
import org.apache.olingo.commons.api.edm.EdmTerm;

public class EdmNavigationPropertyDouble implements EdmNavigationProperty {
  private final String name;

  public EdmNavigationPropertyDouble(final String name) {
    super();
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isCollection() {
    return (boolean) failWithNull();
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
  public EdmEntityType getType() {
    return (EdmEntityType) failWithNull();
  }

  @Override
  public boolean isNullable() {
    return (boolean) failWithNull();
  }

  @Override
  public boolean containsTarget() {
    return (boolean) failWithNull();
  }

  @Override
  public EdmNavigationProperty getPartner() {
    return (EdmNavigationProperty) failWithNull();
  }

  @Override
  public String getReferencingPropertyName(final String referencedPropertyName) {
    return (String) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<EdmReferentialConstraint> getReferentialConstraints() {
    return (List<EdmReferentialConstraint>) failWithNull();
  }

  @Override
  public EdmOnDelete getOnDelete() {
    return (EdmOnDelete) failWithNull();
  }

  private final Object failWithNull() {
    fail();
    return null;
  }

}
