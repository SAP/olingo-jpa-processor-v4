package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAJavaOperation {
  /**
   * @return The Method that implements a function
   */
  public Method getMethod();

  /**
   *
   * @return The constructor to be used to create a new instance
   */
  public <X> Constructor<X> getConstructor();

  /**
   *
   * @param declaredParameter
   * @return
   * @throws ODataJPAModelException
   */
  JPAParameter getParameter(final Parameter declaredParameter) throws ODataJPAModelException;

}
