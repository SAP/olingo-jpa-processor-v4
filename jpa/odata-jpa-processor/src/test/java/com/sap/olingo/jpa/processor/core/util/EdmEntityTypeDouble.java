package com.sap.olingo.jpa.processor.core.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.apache.olingo.commons.api.edm.EdmAnnotation;
import org.apache.olingo.commons.api.edm.EdmElement;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.edm.EdmProperty;
import org.apache.olingo.commons.api.edm.EdmTerm;
import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.constants.EdmTypeKind;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;

public class EdmEntityTypeDouble implements EdmEntityType {

  private final String name;
  private final JPAEdmNameBuilder nameBuilder;

  public EdmEntityTypeDouble(final JPAEdmNameBuilder nameBuilder, final String name) {
    this.name = name;
    this.nameBuilder = nameBuilder;
  }

  @Override
  public EdmElement getProperty(final String name) {
    return (EdmElement) failWithNull();
  }

  private final Object failWithNull() {
    fail();
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getPropertyNames() {
    return (List<String>) failWithNull();
  }

  @Override
  public EdmProperty getStructuralProperty(final String name) {
    return (EdmProperty) failWithNull();
  }

  @Override
  public EdmNavigationProperty getNavigationProperty(final String name) {
    return (EdmNavigationProperty) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getNavigationPropertyNames() {
    return (List<String>) failWithNull();
  }

  @Override
  public boolean compatibleTo(final EdmType targetType) {
    return (boolean) failWithNull();
  }

  @Override
  public boolean isOpenType() {
    return (boolean) failWithNull();
  }

  @Override
  public boolean isAbstract() {
    return (boolean) failWithNull();
  }

  @Override
  public FullQualifiedName getFullQualifiedName() {
    return new FullQualifiedName(nameBuilder.getNamespace(), name);
  }

  @Override
  public String getNamespace() {
    return nameBuilder.getNamespace();
  }

  @Override
  public EdmTypeKind getKind() {
    return (EdmTypeKind) failWithNull();
  }

  @Override
  public String getName() {
    return name;
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

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getKeyPredicateNames() {
    return (List<String>) failWithNull();
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<EdmKeyPropertyRef> getKeyPropertyRefs() {
    return (List<EdmKeyPropertyRef>) failWithNull();
  }

  @Override
  public EdmKeyPropertyRef getKeyPropertyRef(final String keyPredicateName) {
    return (EdmKeyPropertyRef) failWithNull();
  }

  @Override
  public boolean hasStream() {
    return (boolean) failWithNull();
  }

  @Override
  public EdmEntityType getBaseType() {
    return (EdmEntityType) failWithNull();
  }

}
