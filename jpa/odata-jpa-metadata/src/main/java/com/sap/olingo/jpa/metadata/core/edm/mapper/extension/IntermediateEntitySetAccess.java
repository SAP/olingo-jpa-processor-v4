package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

public interface IntermediateEntitySetAccess extends JPAElement {
  /**
   * Enables to add or overwrite annotations to an entity set, e.g. because the type of annotation is not enabled via
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation EdmAnnotation} or they should be changed
   * during runtime.
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);
}
