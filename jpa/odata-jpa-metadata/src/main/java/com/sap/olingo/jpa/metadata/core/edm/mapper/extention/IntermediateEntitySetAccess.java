package com.sap.olingo.jpa.metadata.core.edm.mapper.extention;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlAnnotation;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;

public interface IntermediateEntitySetAccess extends JPAElement {
  /**
   * Enables to add annotations to a property, e.g. because the type of annotation is not enabled via
   * {@link com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation EdmAnnotation} or should be during runtime
   * @param annotations
   */
  public void addAnnotations(final List<CsdlAnnotation> annotations);

  /**
   * @deprecated (0.3.10, Overriding the external name not working correctly. Create an {@link JPAEdmNameBuilder} instead )
   */
  @Deprecated
  void setExternalName(String externalName);
}
