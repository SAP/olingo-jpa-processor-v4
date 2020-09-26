package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

public interface IntermediateEntityContainerAccess {
  /**
   * Enables to add annotations to the entity container
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);
}
