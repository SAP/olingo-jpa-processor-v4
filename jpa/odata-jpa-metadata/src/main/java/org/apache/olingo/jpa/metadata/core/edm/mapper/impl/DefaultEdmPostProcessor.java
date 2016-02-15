package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

class DefaultEdmPostProcessor extends org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor {

  @Override
  public void processNavigationProperty(IntermediateNavigationPropertyAccess property,
      String jpaManagedTypeClassName) {}

  @Override
  public void processProperty(IntermediatePropertyAccess property, String jpaManagedTypeClassName) {}

}
