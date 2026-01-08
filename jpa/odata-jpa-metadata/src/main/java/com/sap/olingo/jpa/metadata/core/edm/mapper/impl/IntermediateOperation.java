package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.Arrays;
import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

abstract class IntermediateOperation extends IntermediateModelElement implements JPAOperation {
  private List<String> userGroups;

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

  protected void determineUserGroups(EdmVisibleFor jpaUserGroups) {
    if (jpaUserGroups != null)
      userGroups = Arrays.stream(jpaUserGroups.value()).toList();
    else
      userGroups = List.of();
  }

  @Override
  public List<String> getUserGroups() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return userGroups;
  }

}