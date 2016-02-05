package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

public interface JPADescriptionAttribute extends JPAAttribute {

  public boolean isLocationJoin();

  public JPAAttribute getDescriptionAttribute();

  public String getLocaleFieldName();

}
