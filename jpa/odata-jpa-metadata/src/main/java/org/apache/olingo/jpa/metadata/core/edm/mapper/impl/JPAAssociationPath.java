package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.List;

import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAStructuredType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAssociationPath {

  String PATH_SEPERATOR = "/";

  String getAlias();

  List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException;

  JPAAssociationAttribute getLeaf();

  List<JPAElement> getPath();

  JPAStructuredType getTargetType();

  JPAStructuredType getSourceType();

}