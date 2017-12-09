package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static javax.persistence.metamodel.Type.PersistenceType.EMBEDDABLE;

import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.Type;
import javax.persistence.metamodel.Type.PersistenceType;

import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateCollectionProperty extends IntermediateProperty {

  IntermediateCollectionProperty(final JPAEdmNameBuilder nameBuilder,
      final PluralAttribute<?, ?, ?> jpaAttribute, final IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, jpaAttribute, schema);
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    super.lazyBuildEdmItem();
    edmProperty.setCollection(true);
  }

  @Override
  public boolean isComplex() {
    return getRowType().getPersistenceType() == EMBEDDABLE ? true : false;
  }

  @Override
  FullQualifiedName determineType() throws ODataJPAModelException {
    return determineTypeByPersistanceType(getRowType().getPersistenceType());
  }

  @Override
  public boolean isEtag() {
    return false;
  }

  @Override
  void determineStructuredType() {
    if (getRowType().getPersistenceType() == PersistenceType.EMBEDDABLE)
      type = schema.getStructuredType((PluralAttribute<?, ?, ?>) jpaAttribute);
    else
      type = null;
  }

  @Override
  Class<?> determineEntityType() {
    return getRowType().getJavaType();
  }

  @Override
  public boolean isKey() {
    return false;
  }

  @Override
  public boolean isAssociation() {
    return false;
  }

  @Override
  public boolean isSearchable() {
    return false;
  }

  @Override
  void determineStreamInfo() throws ODataJPAModelException {
    // Stream properties not supported
  }

  @Override
  void determineIsVersion() {
    isVersion = false; // Version is always false
  }

  @Override
  String getDeafultValue() throws ODataJPAModelException {
    // No defaults for collection properties
    return null;
  }

  @Override
  boolean isStream() {
    // OData Version 4.0. Part 3: Common Schema Definition Language (CSDL) Plus Errata 03:
    // Edm.Stream, or a type definition whose underlying type is Edm.Stream, cannot be used in collections or for
    // non-binding parameters to functions or actions.
    return false;
  }

  private Type<?> getRowType() {
    return ((PluralAttribute<?, ?, ?>) jpaAttribute).getElementType();
  }
}
