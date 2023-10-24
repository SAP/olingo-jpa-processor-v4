package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

import jakarta.persistence.metamodel.Metamodel;

public final class JPAServiceDocumentFactory {

  private final JPAEdmNameBuilder nameBuilder;
  private final Metamodel jpaMetamodel;
  private final JPAEdmMetadataPostProcessor postProcessor;
  private final String[] packageName;
  private final List<AnnotationProvider> annotationProvider;

  public JPAServiceDocumentFactory(final JPAEdmNameBuilder nameBuilder, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor, final String[] packageName,
      final List<AnnotationProvider> annotationProvider) {
    super();
    this.nameBuilder = nameBuilder;
    this.jpaMetamodel = jpaMetamodel;
    this.postProcessor = postProcessor;
    this.packageName = packageName;
    this.annotationProvider = annotationProvider;
  }

  /**
   * Late creation of the service document. A service document contains at least one schema and a container.
   * @return
   * @throws ODataJPAModelException
   */
  public JPAServiceDocument getServiceDocument() throws ODataJPAModelException {
    return new IntermediateServiceDocument(nameBuilder, jpaMetamodel, postProcessor, packageName, annotationProvider);
  }
}
