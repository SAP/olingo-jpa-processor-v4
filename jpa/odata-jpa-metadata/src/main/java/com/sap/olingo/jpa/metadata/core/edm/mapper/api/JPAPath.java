package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

/**
 * A path within an JPA entity to an attribute.
 * @author Oliver Grande
 *
 */
public interface JPAPath extends Comparable<JPAPath> {

  final String PATH_SEPARATOR = "/";

  /**
   * External unique identifier for a path. Two path are seen as equal if they have the same alias
   * @return
   */
  String getAlias();

  /**
   * @return the name of the data base table/view column of the leaf of a path
   */
  String getDBFieldName();

  /**
   * @return the last element of a path
   */
  JPAAttribute getLeaf();

  /**
   * @return all elements of a path
   */
  List<JPAElement> getPath();

  /**
   * @return true if the leaf of the path shall be ignored
   */
  boolean ignore();

  /**
   * Returns true in case the leaf of the path is part of one of the provided groups or none of the path elements is
   * annotated with EdmVisibleFor. The leaf is seen as a member of a group in case its EdmVisibleFor annotation contains
   * the group or the groups is mentioned at any other element of the path. <br>
   * <b>Note:</b> Based on this inheritance of EdmVisibleFor a path is seen as inconsistent if multiple elements are
   * annotated and the difference of the set of groups is not empty.
   * @return
   */
  public boolean isPartOfGroups(final List<String> groups);

  /**
   * 
   * @return True in case at least on of the elements of the path is a transient property
   */
  public boolean isTransient();

}