package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.annotation.Nonnull;

import jakarta.persistence.metamodel.Attribute;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateVirtualProperty extends IntermediateProperty {

  IntermediateVirtualProperty(final JPAEdmNameBuilder nameBuilder, final Attribute<?, ?> jpaAttribute,
      final IntermediateSchema schema, final String dbColumnName, @Nonnull final Class<?> dbType)
      throws ODataJPAModelException {
    super(nameBuilder, jpaAttribute, schema);
    this.dbFieldName = dbColumnName;
    this.dbType = dbType;
    this.conversionRequired = true;
  }

  @Override
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isEtag() {
    return false;
  }

  @Override
  public boolean isSearchable() {
    return false;
  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public boolean isVirtual() {
    return true;
  }

  @Override
  public boolean ignore() {
    return true;
  }

  @Override
  public boolean isAssociation() {
    return false;
  }

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public boolean isComplex() {
    return false;
  }

  @Override
  public boolean isKey() {
    return false;
  }

  @Override
  void checkConsistency() throws ODataJPAModelException {
    // Virtual Property are always consistent.
  }

  @Override
  Class<?> determinePropertyType() {
    return null;
  }

  @Override
  void determineIsVersion() {
    // Virtual Property are never version property.
    isVersion = false;
  }

  @Override
  void determineStreamInfo() throws ODataJPAModelException {
    // Virtual Property are no stream properties.
  }

  @Override
  void determineStructuredType() {
    type = null;
  }

  @Override
  FullQualifiedName determineType() throws ODataJPAModelException {
    return null;
  }

  @Override
  String getDefaultValue() throws ODataJPAModelException {
    return null;
  }

  @Override
  boolean isStream() {
    return false;
  }

  @Override
  boolean hasUserGroupRestriction() {
    return false;
  }

  @Override
  public Class<?> getJavaType() {
    return dbType;
  }
}
