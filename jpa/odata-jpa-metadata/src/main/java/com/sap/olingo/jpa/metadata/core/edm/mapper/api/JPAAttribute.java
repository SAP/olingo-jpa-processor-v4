package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransientPropertyCalculator;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAttribute extends JPAElement {
  /**
   * Returns an instance of the converter defined at the attribute, in case an explicit conversion is required. That is,
   * Olingo does not support the Java type. In that case the JPA Processor converts the attribute into the DB type
   * before handing over the value to Olingo.
   * @param <X> the type of the entity attribute
   * @param <Y> the type of the database column
   * @return
   */
  public <X, Y extends Object> AttributeConverter<X, Y> getConverter();

  /**
   * Returns an instance of the converter defined at the attribute independent from the ability of Olingo.
   * @param <X> the type of the entity attribute
   * @param <Y> the type of the database column
   * @return
   */
  public <X, Y extends Object> AttributeConverter<X, Y> getRawConverter();

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
  public List<String> getProtectionPath(String claimName) throws ODataJPAModelException;

  /**
   * @return The JAVA type that is used to derive an OData (Edm) type. This could be a simple JAVA type like Integer or
   * String or a complex type in case of embedded attributes of navigation attributes. For simple attributes the type is
   * usually the type given in the entity definition. In case this is not supported by Olingo and an
   * {@link AttributeConverter} is provided, the database type is used.
   *
   */
  public Class<?> getType();

  /**
   * @return For simple attributes the JAVA type that can be expected to be returned from the database, otherwise null.
   *
   */
  public @CheckForNull Class<?> getDbType();

  public boolean isAssociation();

  /**
   * True if a to n association is involved
   * @return
   */
  public boolean isCollection();

  public boolean isComplex();

  /**
   * True if the property has an enum as type
   * @return
   */
  public boolean isEnum();

  public boolean isEtag();

  public boolean isKey();

  public boolean isSearchable();

  public boolean hasProtection();

  public boolean isTransient();

  /**
   *
   * @param <T>
   * @return
   * @throws ODataJPAModelException
   */
  public <T extends EdmTransientPropertyCalculator<?>> Constructor<T> getCalculatorConstructor()
      throws ODataJPAModelException;

  /**
   * @return A list of path pointing to the properties that are required to calculate the value of this property
   */
  List<String> getRequiredProperties();
}
