package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAFunction extends JPAOperation {

  /**
   * 
   * @return List of import parameter
   * @throws ODataJPAModelException
   */
  public List<JPAParameter> getParameter() throws ODataJPAModelException;

  /**
   * 
   * @param internalName
   * @return
   * @throws ODataJPAModelException
   */
  public JPAParameter getParameter(String internalName) throws ODataJPAModelException;

  /**
   * 
   * @return The type of function
   */
  public EdmFunctionType getFunctionType();

  public boolean isBound() throws ODataJPAModelException;
}
