package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAssociationPath {

  String PATH_SEPERATOR = "/";

  String getAlias();

  List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException;

  List<JPAPath> getLeftColumnsList() throws ODataJPAModelException;

  List<JPAPath> getRightColumnsList() throws ODataJPAModelException;

  JPAAssociationAttribute getLeaf();

  List<JPAElement> getPath();

  JPAStructuredType getTargetType();

  JPAStructuredType getSourceType();

  boolean isCollection();

  JPAAssociationAttribute getPartner();

  JPAJoinTable getJoinTable();

  /**
   * Only available if a Join Table was used
   * @return
   * @throws ODataJPAModelException
   */
  List<JPAPath> getInverseLeftJoinColumnsList() throws ODataJPAModelException;
}