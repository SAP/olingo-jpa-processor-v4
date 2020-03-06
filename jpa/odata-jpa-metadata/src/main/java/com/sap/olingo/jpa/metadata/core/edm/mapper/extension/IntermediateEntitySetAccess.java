package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

public interface IntermediateEntitySetAccess extends JPAElement {
  /**
   * Enables to add annotations to a property, e.g. because the type of annotation is not enabled via
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation EdmAnnotation} or should be during runtime
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);

  /**
   * Enables a rename external, OData, name of an entity set.
   * @param externalName
   */
  void setExternalName(String externalName);
}