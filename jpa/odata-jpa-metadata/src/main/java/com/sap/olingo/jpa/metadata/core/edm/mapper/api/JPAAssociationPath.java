package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAssociationPath {

  String getAlias();

  /**
   * Only available if a Join Table was used
   * @return
   * @throws ODataJPAModelException
   */
  List<JPAPath> getInverseLeftJoinColumnsList() throws ODataJPAModelException;

  List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException;

  JPAJoinTable getJoinTable();

  JPAAssociationAttribute getLeaf();

  List<JPAPath> getLeftColumnsList() throws ODataJPAModelException;

  JPAAssociationAttribute getPartner();

  List<JPAElement> getPath();

  List<JPAPath> getRightColumnsList() throws ODataJPAModelException;

  JPAStructuredType getSourceType();

  JPAStructuredType getTargetType();

  /**
   * @return True if the target entity is linked via a join table
   */
  boolean hasJoinTable();

  boolean isCollection();
}