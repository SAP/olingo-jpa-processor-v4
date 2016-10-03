package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

class DefaultEdmPostProcessor extends org.apache.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor {

  @Override
  public void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
      final String jpaManagedTypeClassName) {}

  @Override
  public void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName) {}

  @Override
  public void provideReferences(IntermediateReferenceList references) {

  }
}
