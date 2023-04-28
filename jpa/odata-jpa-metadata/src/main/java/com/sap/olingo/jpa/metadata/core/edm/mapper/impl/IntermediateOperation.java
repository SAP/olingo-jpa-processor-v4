package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateOperation extends IntermediateModelElement {

  IntermediateOperation(final JPAEdmNameBuilder nameBuilder, final String internalName,
      final IntermediateAnnotationInformation annotationInfo) {
    super(nameBuilder, internalName, annotationInfo);
  }

  abstract boolean hasImport();

  abstract boolean isBound() throws ODataJPAModelException;

  protected Integer nullIfNotSet(final Integer number) {
    if (number != null && number > -1)
      return number;
    return null;
  }

}