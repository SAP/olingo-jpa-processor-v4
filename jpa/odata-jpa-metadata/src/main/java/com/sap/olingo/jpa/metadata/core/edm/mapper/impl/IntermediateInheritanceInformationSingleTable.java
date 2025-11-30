package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceInformation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateInheritanceInformationSingleTable implements JPAInheritanceInformation {

  @Override
  public JPAInheritanceType getInheritanceType() {
    return JPAInheritanceType.SINGLE_TABLE;
  }

  @Override
  public List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException {
    return List.of();
  }

  @Override
  public List<JPAOnConditionItem> getReversedJoinColumnsList() throws ODataJPAModelException {
    return List.of();
  }

}
