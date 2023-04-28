package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.edmx.EdmxReference;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AnnotationProvider;
import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.JPAReferences;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.IntermediateReferenceList;

final class IntermediateAnnotationInformation {
  private final List<AnnotationProvider> annotationProvider;
  private final IntermediateReferences references;

  IntermediateAnnotationInformation(final List<AnnotationProvider> annotationProvider,
      final IntermediateReferences references) {
    super();
    this.annotationProvider = annotationProvider;
    this.references = references;
  }

  IntermediateAnnotationInformation(final List<AnnotationProvider> annotationProvider) {
    this(annotationProvider, new IntermediateReferences());
  }

  List<AnnotationProvider> getAnnotationProvider() {
    return annotationProvider;
  }

  JPAReferences getReferences() {
    return references;
  }

  IntermediateReferenceList asReferenceList() {
    return references;
  }

  /**
   * Returns the schemas of the provided references
   */
  List<CsdlSchema> getSchemas() {
    return references.getSchemas();
  }

  List<EdmxReference> getEdmReferences() {
    return references.getEdmReferences();
  }

  @Override
  public String toString() {
    return "IntermediateAnnotationInformation [references=" + references + ", annotationProvider=" + annotationProvider
        + "]";
  }
}
