package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Arrays;
import java.util.Collection;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

public class IntermediateOperationHelper {

  private IntermediateOperationHelper() {
    // Must not create instances
  }

  @SuppressWarnings("unchecked")
  static <T> Constructor<T> determineConstructor(final Method javaFunction) throws ODataJPAModelException {
    Constructor<T> result = null;
    final Constructor<T>[] constructors = (Constructor<T>[]) ((Class<T>) javaFunction.getDeclaringClass())
        .getConstructors();
    for (final Constructor<T> constructor : Arrays.asList(constructors)) {
      final Parameter[] parameters = constructor.getParameters();
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

  static boolean isCollection(final Class<?> declairedReturnType) {
    for (final Class<?> inter : Arrays.asList(declairedReturnType.getInterfaces())) {
      if (inter == Collection.class)
        return true;
    }
    return false;
  }

  static FullQualifiedName determineReturnType(final ReturnType definedReturnType, final Class<?> declairedReturnType,
      final IntermediateSchema schema, final String operationName) throws ODataJPAModelException {

    final IntermediateStructuredType<?> structuredType = schema.getStructuredType(declairedReturnType);
    if (structuredType != null)
      return structuredType.getExternalFQN();
    else {
      final IntermediateEnumerationType enumType = schema.getEnumerationType(declairedReturnType);
      if (enumType != null) {
        return enumType.getExternalFQN();
      } else if (declairedReturnType.equals(Blob.class) || declairedReturnType.equals(Clob.class)) {
        // The return type '%1$s' used at method '%3$s' is not supported
        throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_NOT_SUPPORTED, declairedReturnType.getName(),
            operationName);
      } else {
        final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedReturnType);
        if (edmType == null)
          throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
              declairedReturnType.getName(), operationName);
        return edmType.getFullQualifiedName();
      }
    }
  }
}
