package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public interface JPAJavaFunction extends JPAFunction {
  /**
   * @return The Method that implements a function
   */
  public Method getMethod();

  /**
   * 
   * @return The constructor to be used to create a new instance
   */
  public Constructor<?> getConstructor();

}
