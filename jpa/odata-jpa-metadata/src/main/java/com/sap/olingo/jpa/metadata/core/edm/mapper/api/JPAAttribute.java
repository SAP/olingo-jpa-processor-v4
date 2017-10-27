package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAttribute extends JPAElement {

  public <X, Y extends Object> AttributeConverter<X, Y> getConverter();

  public JPAStructuredType getStructuredType() throws ODataJPAModelException;

  public Class<?> getType();

  public boolean isComplex();

  public boolean isKey();

  public boolean isAssociation();

  public boolean isSearchable();

  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException;

  public CsdlAbstractEdmItem getProperty() throws ODataJPAModelException;
}
