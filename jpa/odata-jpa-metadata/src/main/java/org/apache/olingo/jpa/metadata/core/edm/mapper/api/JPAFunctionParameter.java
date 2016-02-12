package org.apache.olingo.jpa.metadata.core.edm.mapper.api;

public interface JPAFunctionParameter {

  public String getDBName();

  public Class<?> getType();

  public String getName();

  public Integer maxLength();

  public Integer precision();

  public Integer scale();

}
