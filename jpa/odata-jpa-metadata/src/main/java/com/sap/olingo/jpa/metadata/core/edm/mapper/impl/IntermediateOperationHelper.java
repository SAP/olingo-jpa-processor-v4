package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class IntermediateOperationHelper {

  private IntermediateOperationHelper() {
// Must not create instances
  }

  static Constructor<?> determineConstructor(Method javaFunction) throws ODataJPAModelException {
    Constructor<?> result = null;
    Constructor<?>[] constructors = javaFunction.getDeclaringClass().getConstructors();
    for (Constructor<?> constructor : Arrays.asList(constructors)) {
      Parameter[] parameters = constructor.getParameters();
      if (parameters.length == 0)
        result = constructor;
      else if (parameters.length == 1 && parameters[0].getType() == EntityManager.class) {
        result = constructor;
        break;
      }
    }
    if (result == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_CONSTRUCTOR_MISSING, javaFunction
          .getClass().getName());
    return result;
  }

  static boolean isCollection(Class<?> declairedReturnType) {
    for (Class<?> inter : Arrays.asList(declairedReturnType.getInterfaces())) {
      if (inter == Collection.class)
        return true;
    }
    return false;
  }
}
