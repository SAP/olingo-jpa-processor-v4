package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 *
 *
 *
 * 2025-10-21
 *
 */
public interface JPAInheritanceInformation {
  default JPAInheritanceType getInheritanceType() {
    return JPAInheritanceType.NON;
  }

  /**
   *
   * @return The join condition in case of inheritance by join
   * @throws ODataJPAModelException
   */
  List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException;

  List<JPAOnConditionItem> getReversedJoinColumnsList() throws ODataJPAModelException;
}
