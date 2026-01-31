package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import jakarta.persistence.metamodel.Metamodel;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public final class JPAServiceDocumentFactory {

  /**
   * Late creation of the service document. A service document contains at least one schema and a container.
   * @param wrapperAvailable
   * @return
   * @throws ODataJPAModelException
   */
  public JPAServiceDocument getServiceDocument(final JPAEdmNameBuilder nameBuilder, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName,
      final List<AnnotationProvider> annotationProvider, final boolean wrapperAvailable) throws ODataJPAModelException {
    return new IntermediateServiceDocument(nameBuilder, jpaMetamodel, postProcessor, packageName, annotationProvider,
        wrapperAvailable);
  }

  public JPAServiceDocument asUserGroupRestricted(final JPAServiceDocument serviceDocument,
      final List<String> userGroups, final boolean hideRestrictedProperties) throws ODataJPAModelException {
    if (serviceDocument instanceof final IntermediateServiceDocument intermediate)
      return intermediate.asUserGroupRestricted(userGroups, hideRestrictedProperties);
    return serviceDocument;
  }
}
