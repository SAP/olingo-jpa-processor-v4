package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
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

  String getExternalName();
}
