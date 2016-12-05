package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAssociationPath {

  String PATH_SEPERATOR = "/";

  String getAlias();

  List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException;

  JPAAssociationAttribute getLeaf();

  List<JPAElement> getPath();

  JPAStructuredType getTargetType();

  JPAStructuredType getSourceType();

}