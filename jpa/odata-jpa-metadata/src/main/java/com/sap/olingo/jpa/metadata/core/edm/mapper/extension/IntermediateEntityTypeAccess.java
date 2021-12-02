package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

public interface IntermediateEntityTypeAccess extends IntermediateModelItemAccess {
  /**
   * Enables to add annotations to the entity container
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);
}
