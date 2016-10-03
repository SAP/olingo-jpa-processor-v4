package org.apache.olingo.jpa.metadata.api;

import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateNavigationPropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediatePropertyAccess;
import org.apache.olingo.jpa.metadata.core.edm.mapper.extention.IntermediateReferenceList;

public abstract class JPAEdmMetadataPostProcessor {

  public abstract void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
      final String jpaManagedTypeClassName);

  /**
   * 
   * @param property
   * @param jpaManagedTypeClassName
   * @return
   */
  // TODO Documentation
  public abstract void processProperty(final IntermediatePropertyAccess property, final String jpaManagedTypeClassName);

  public abstract void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException;
}
