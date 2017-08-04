package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

public interface JPAFunctionParameter extends JPAFunctionParamaterFacet {

  /**
   * Name of the parameter at the UDF or the java method
   * @return
   */
  public String getInternalName();

  /**
   * Externaly used name
   * @return
   */
  public String getName();

}
