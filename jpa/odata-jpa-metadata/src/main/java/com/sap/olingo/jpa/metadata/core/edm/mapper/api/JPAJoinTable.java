package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

public interface JPAJoinTable {

  public String getTableName();

  public String getAlias(String dbFieldName);

  public String getInverseAlias(String dbFieldName);

}
