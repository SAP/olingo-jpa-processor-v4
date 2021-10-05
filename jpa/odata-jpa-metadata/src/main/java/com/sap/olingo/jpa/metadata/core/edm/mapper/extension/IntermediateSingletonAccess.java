package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

/**
 *
 * @author D023143
 * @since 1.0.3
 */
public interface IntermediateSingletonAccess extends JPAElement {
  /**
   * Enables to add or change annotations of a singleton, e.g. because the type of annotation is not enabled via
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation EdmAnnotation} or they should be changed
   * during runtime.
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);
}
