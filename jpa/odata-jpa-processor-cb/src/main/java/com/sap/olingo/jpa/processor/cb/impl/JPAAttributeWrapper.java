package com.sap.olingo.jpa.processor.cb.impl;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.AttributeConverter;
import javax.persistence.criteria.Selection;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class JPAAttributeWrapper implements JPAAttribute {
  private final Selection<?> selection;

  public JPAAttributeWrapper(final Selection<?> sel) {
    this.selection = sel;
  }

  @Override
  public FullQualifiedName getExternalFQN() {
    return null;
  }

  @Override
  public String getExternalName() {
    return null;
  }

  @Override
  public String getInternalName() {
    return null;
  }

  @Override
  public <X, Y> AttributeConverter<X, Y> getConverter() {
    return null;
  }

  @Override
  public <X, Y> AttributeConverter<X, Y> getRawConverter() {
    return null;
  }

  @Override
  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException {
    return null;
  }

  @Override
  public CsdlAbstractEdmItem getProperty() throws ODataJPAModelException {
    return null;
  }

  @Override
  public JPAStructuredType getStructuredType() throws ODataJPAModelException {
    return null;
  }

  @Override
  public Set<String> getProtectionClaimNames() {
    return Collections.emptySet();
  }

  @Override
  public List<String> getProtectionPath(final String claimName) throws ODataJPAModelException {
    return Collections.emptyList();
  }

  @Override
  public Class<?> getType() {
    return selection.getJavaType();
  }

  @Override
  public Class<?> getDbType() {
    return selection.getJavaType();
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
  public boolean isEnum() {
    return false;
  }

  @Override
  public boolean isEtag() {
    return false;
  }

  @Override
  public boolean isKey() {
    return false;
  }

  @Override
  public boolean isSearchable() {
    return false;
  }

  @Override
  public boolean hasProtection() {
    return false;
  }

  @Override
  public boolean isTransient() {
    return false;
  }

  @Override
  public <T extends EdmTransientPropertyCalculator<?>> Constructor<T> getCalculatorConstructor() {
    return null;
  }

  @Override
  public List<String> getRequiredProperties() {
    return Collections.emptyList();
  }
}