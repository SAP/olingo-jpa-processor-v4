package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

final class DefaultEdmPostProcessor extends JPAEdmMetadataPostProcessor {

  @Override
  public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
      final String jpaManagedTypeClassName) {
    // Default shall do nothing
  }

  @Override
  public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {
    // Default shall do nothing
  }

  @Override
  public void provideReferences(IntermediateReferenceList references) {
    // Default shall do nothing
  }

  @Override
  public void processEntityType(IntermediateEntityTypeAccess entity) {
    // Default shall do nothing
  }
}
