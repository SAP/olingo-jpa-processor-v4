package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAttribute extends JPAElement {

  public AttributeConverter<?, ?> getConverter();

  public JPAStructuredType getStructuredType();

  public Class<?> getType();

  public boolean isComplex();

  public boolean isKey();

  public boolean isAssociation();

  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException;

  public CsdlAbstractEdmItem getProperty() throws ODataJPAModelException;
}
