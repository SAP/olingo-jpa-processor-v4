package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.persistence.metamodel.Metamodel;

import com.sap.olingo.jpa.metadata.api.JPAEdmMetadataPostProcessor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public final class JPAServiceDocumentFactory {

  private final String namespace;
  private final Metamodel jpaMetamodel;
  private final JPAEdmMetadataPostProcessor postProcessor;

  public JPAServiceDocumentFactory(final String namespace, final Metamodel jpaMetamodel,
      final JPAEdmMetadataPostProcessor postProcessor) {
    super();
    this.namespace = namespace;
    this.jpaMetamodel = jpaMetamodel;
    this.postProcessor = postProcessor;
  }

  public JPAServiceDocument getServiceDocument() throws ODataJPAModelException {
    return new IntermediateServiceDocument(namespace, jpaMetamodel, postProcessor);
  }
}
