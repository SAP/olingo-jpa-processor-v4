package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceInformation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAInheritanceType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateInheritanceInformationJoinTable implements JPAInheritanceInformation {
  private final List<JPAOnConditionItem> joinColumns;

  IntermediateInheritanceInformationJoinTable(List<JPAOnConditionItem> joinColumns) {
    this.joinColumns = joinColumns;
  }

  @Override
  public JPAInheritanceType getInheritanceType() {
    return JPAInheritanceType.JOIN_TABLE;
  }

  @Override
  public List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException {
    return joinColumns;
  }

}
