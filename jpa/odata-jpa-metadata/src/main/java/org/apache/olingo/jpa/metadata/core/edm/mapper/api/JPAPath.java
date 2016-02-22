package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

public interface JPAPath extends Comparable<JPAPath> {

  String PATH_SEPERATOR = "/";

  String getAlias();

  String getDBFieldName();

  JPAAttribute getLeaf();

  List<JPAElement> getPath();

  boolean ignore();

}