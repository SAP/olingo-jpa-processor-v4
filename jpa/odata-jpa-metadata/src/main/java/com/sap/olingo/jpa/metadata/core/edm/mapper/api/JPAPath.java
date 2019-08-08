package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

public interface JPAPath extends Comparable<JPAPath> {

  String PATH_SEPERATOR = "/";

  String getAlias();

  String getDBFieldName();

  JPAAttribute getLeaf();

  List<JPAElement> getPath();

  boolean ignore();

  /**
   * Returns true in case the leaf of the is part of one of the provided groups or none of the path elements is
   * annotated with EdmVisibleFor. The leaf is seen as a member of a group in case its EdmVisibleFor annotation contains
   * the group or the groups is mentioned at any other element of the path. <br>
   * <b>Note:</b> Based on this inheritance of EdmVisibleFor a path is seen as inconsistent if multiple elements are
   * annotated and the difference of the set of groups is not empty.
   * @return
   */
  public boolean isPartOfGroups(final List<String> groups);

}