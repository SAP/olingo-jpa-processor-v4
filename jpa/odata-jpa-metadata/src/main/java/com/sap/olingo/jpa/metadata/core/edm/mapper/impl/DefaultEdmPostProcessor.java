package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

final class DefaultEdmPostProcessor extends com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor {

  @Override
  public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
      final String jpaManagedTypeClassName) {}

  @Override
  public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {}

  @Override
  public void provideReferences(IntermediateReferenceList references) {}

  @Override
  public void processEntityType(IntermediateEntityTypeAccess entity) {}
}
