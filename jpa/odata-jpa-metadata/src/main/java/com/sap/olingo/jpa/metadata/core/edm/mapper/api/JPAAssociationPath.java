package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import javax.annotation.CheckForNull;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.ODataNavigationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAAssociationPath extends ODataNavigationPath {

  String getAlias();

  /**
   * Only available if a Join Table was used
   * @return
   * @throws ODataJPAModelException
   */
  List<JPAPath> getInverseLeftJoinColumnsList() throws ODataJPAModelException;

  List<JPAOnConditionItem> getJoinColumnsList() throws ODataJPAModelException;

  /**
   * Check with {@link JPAAssociationPath#hasJoinTable()} if an join table exists
   * @return the join table representation if present
   */
  JPAJoinTable getJoinTable();

  JPAAssociationAttribute getLeaf();

  /**
   *
   * @return
   * @throws ODataJPAModelException
   */
  List<JPAPath> getLeftColumnsList() throws ODataJPAModelException;

  @CheckForNull
  JPAAssociationAttribute getPartner();

  List<JPAElement> getPath();

  /**
   *
   * @return
   * @throws ODataJPAModelException
   */
  List<JPAPath> getRightColumnsList() throws ODataJPAModelException;

  JPAStructuredType getSourceType();

  JPAStructuredType getTargetType();

  /**
   * @return True if the target entity is linked via a join table
   */
  boolean hasJoinTable();

  boolean isCollection();

  List<JPAPath> getForeignKeyColumns() throws ODataJPAModelException;
}