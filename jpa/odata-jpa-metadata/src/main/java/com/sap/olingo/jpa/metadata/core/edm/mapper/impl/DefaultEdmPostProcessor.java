package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;

class DefaultEdmPostProcessor extends com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor {

  @Override
  public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
      final String jpaManagedTypeClassName) {}

  @Override
  public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {}

}
