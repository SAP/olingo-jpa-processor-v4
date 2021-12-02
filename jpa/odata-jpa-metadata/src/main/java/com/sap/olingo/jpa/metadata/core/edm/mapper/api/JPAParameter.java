package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

public interface JPAParameter extends JPAParameterFacet {

  /**
   * Name of the parameter at the UDF or the java method
   * @return
   */
  public String getInternalName();

  /**
   * Externally used name
   * @return
   */
  public String getName();

}
