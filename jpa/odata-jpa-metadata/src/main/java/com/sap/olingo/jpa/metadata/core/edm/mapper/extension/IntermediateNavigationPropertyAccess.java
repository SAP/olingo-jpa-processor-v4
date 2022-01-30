package com.sap.olingo.jpa.metadata.core.edm.mapper.extension;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;
import org.apache.olingo.commons.api.edm.provider.CsdlOnDelete;

public interface IntermediateNavigationPropertyAccess extends IntermediateModelItemAccess {
  public void setOnDelete(CsdlOnDelete onDelete);

  /**
   * Enables to add annotations to a navigation property, e.g. because the type of annotation is not enabled via
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation EdmAnnotation} or should be changed during
   * runtime
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);

}
