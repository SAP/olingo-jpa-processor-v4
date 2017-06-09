package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAFunction {

  /**
   * 
   * @return List of import parameter
   * @throws ODataJPAModelException
   */
  public List<JPAFunctionParameter> getParameter() throws ODataJPAModelException;

  /**
   * 
   * @param internalName
   * @return
   * @throws ODataJPAModelException
   */
  public JPAFunctionParameter getParameter(String internalName) throws ODataJPAModelException;

  /**
   * 
   * @return The return or result parameter of the function
   */
  public JPAFunctionResultParameter getResultParameter();

  /**
   * 
   * @return The type of function
   */
  public EdmFunctionType getFunctionType();
}
