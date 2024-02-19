package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AliasAccess;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.PropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.TermAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * Gives access to OData annotations
 *
 * @author Oliver Grande
 * @since 1.1.1
 * 01.03.2023
 */
public interface JPAAnnotatable {
  /**
   * Returns a OData annotation. E.g. <code>getAnnotation("Capabilities", "FilterRestrictions")</code> will return the
   * filter restrictions as maintained for the annotatable type
   * @param alias of the vocabulary.
   * @param term of the annotation in question.
   * @return
   * @throws ODataJPAModelException
   */
  @CheckForNull
  CsdlAnnotation getAnnotation(@Nonnull String alias, @Nonnull String term) throws ODataJPAModelException;

  /**
   * Returns the value of a given property of an annotation. E.g.,
   * <code>getAnnotationValue("Capabilities", "FilterRestrictions", "filterable") </code>
   * <p>
   * The value is returned as instance of corresponding type, with the following features
   * <ul>
   * <li>Enumerations are returned as strings</li>
   * <li>Path are returned as {@link JPAPath}</li>
   * <li>Navigation path are returned as {@link JPAAssociationPath}</li>
   * </ul>
   * @param alias of the vocabulary.
   * @param term of the annotation in question.
   * @param property the value is requested for.
   * @return The value of the property
   * @throws ODataJPAModelException
   * @since 2.1.0
   */
  @CheckForNull
  Object getAnnotationValue(@Nonnull String alias, @Nonnull String term, @Nonnull String property)
      throws ODataJPAModelException;

  /**
   * Returns the value of a given property of an annotation. E.g.,
   * <code>getAnnotationValue("Capabilities", "FilterRestrictions", "filterable") </code>
   * <p>
   * The value is returned as instance of corresponding type, with the following features
   * <ul>
   * <li>Enumerations are returned as strings</li>
   * <li>Path are returned as {@link JPAPath}</li>
   * <li>Navigation path are returned as {@link JPAAssociationPath}</li>
   * </ul>
   * @param <T> Java type of annotation.
   * @param alias of the vocabulary.
   * @param term of the annotation in question.
   * @param propertyName the value is requested for.
   * @param type java type of property e.g., Boolean.class.
   * @return
   * @throws ODataJPAModelException
   * @since 2.1.0
   */
  @SuppressWarnings("unchecked")
  @CheckForNull
  default <T> T getAnnotationValue(@Nonnull final String alias, @Nonnull final String term,
      @Nonnull final String property, @Nonnull final Class<?> type) throws ODataJPAModelException {
    return (T) getAnnotationValue(alias, term, property);
  }

  /**
   * Returns the value of a given property of an annotation. E.g.,
   * <code>getAnnotationValue("Capabilities", "FilterRestrictions", "filterable") </code>
   * <p>
   * The value is returned as instance of corresponding type, with the following features
   * <ul>
   * <li>Enumerations are returned as strings</li>
   * <li>Path are returned as {@link JPAPath}</li>
   * <li>Navigation path are returned as {@link JPAAssociationPath}</li>
   * </ul>
   * @param <T> Java type of annotation.
   * @param alias of the vocabulary.
   * @param term of the annotation in question.
   * @param property the value is requested for.
   * @param type java type of property e.g., Boolean.class.
   * @return
   * @throws ODataJPAModelException
   * @since 2.1.0
   */
  @CheckForNull
  default <T> T getAnnotationValue(@Nonnull final AliasAccess alias, @Nonnull final TermAccess term,
      @Nonnull final PropertyAccess property, @Nonnull final Class<T> type) throws ODataJPAModelException {
    return getAnnotationValue(alias.alias(), term.term(), property.property(), type);
  }

  String getExternalName();
}
