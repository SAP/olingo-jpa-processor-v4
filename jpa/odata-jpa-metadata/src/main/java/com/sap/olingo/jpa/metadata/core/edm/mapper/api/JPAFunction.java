package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

public interface JPAFunction {
  /**
   * 
   * @return Name of the function on the database
   */
  public String getDBName();

  /**
   * 
   * @return List of import parameter
   */
  public List<JPAFunctionParameter> getParameter();

  /**
   * 
   * @return The return or result parameter of the function
   */
  public JPAFunctionResultParameter getResultParameter();
}
