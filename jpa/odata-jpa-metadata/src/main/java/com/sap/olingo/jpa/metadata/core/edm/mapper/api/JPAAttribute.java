package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;
import java.util.Set;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAttribute extends JPAElement {

  public <X, Y extends Object> AttributeConverter<X, Y> getConverter();

  public EdmPrimitiveTypeKind getEdmType() throws ODataJPAModelException;

  public CsdlAbstractEdmItem getProperty() throws ODataJPAModelException;

  public JPAStructuredType getStructuredType() throws ODataJPAModelException;

  /**
   * Returns a list of names of the claims that shall be matched with this property
   * @return
   */
  public Set<String> getProtectionClaimNames();

  /**
   * Provides a List of path to the protected attributed
   * @return
   * @throws ODataJPAModelException
   */
  public List<JPAPath> getProtectionPath(String claimName) throws ODataJPAModelException;

  public Class<?> getType();

  public boolean isAssociation();

  /**
   * True is a to n association is involved
   * @return
   */
  public boolean isCollection();

  public boolean isComplex();

  public boolean isEnum();

  public boolean isKey();

  public boolean isSearchable();

  public boolean hasProtection();
}
