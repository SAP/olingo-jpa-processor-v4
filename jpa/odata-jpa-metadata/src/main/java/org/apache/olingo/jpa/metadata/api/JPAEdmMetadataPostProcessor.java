package org.apache.olingo.jpa.metadata.api;

import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

public abstract class JPAEdmMetadataPostProcessor {

  public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
      String jpaManagedTypeClassName) {

  }

  /**
   * 
   * @param property
   * @param jpaManagedTypeClassName
   * @return
   */
  // TODO Documentation
  public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {}
}
