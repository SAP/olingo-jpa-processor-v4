package com.sap.olingo.jpa.metadata.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityContainerAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntitySetAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateEntityTypeAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateNavigationPropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediatePropertyAccess;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateSingletonAccess;

public interface JPAEdmMetadataPostProcessor {
  public default void processEntityContainer(final IntermediateEntityContainerAccess container) {}

  public default void processEntityType(final IntermediateEntityTypeAccess entityType) {}

  public default void processEntitySet(final IntermediateEntitySetAccess entitySet) {}

  public default void processSingleton(final IntermediateSingletonAccess singleton) {}

  public default void processNavigationProperty(final IntermediateNavigationPropertyAccess property,
      final String jpaManagedTypeClassName) {}

  /**
   *
   * @param property
   * @param jpaManagedTypeClassName
   * @return
   */
  public default void processProperty(final IntermediatePropertyAccess property,
      final String jpaManagedTypeClassName) {}

  /**
   * Option to provide references to external CSDL documents. The document must be an XML document
   * @param references List of refernces to external CSDL documents.
   * @throws ODataJPAModelException
   */
  public default void provideReferences(final IntermediateReferenceList references) throws ODataJPAModelException {}
}
