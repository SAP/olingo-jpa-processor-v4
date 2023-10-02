package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.FUNC_CONSTRUCTOR_MISSING;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.FUNC_RETURN_NOT_SUPPORTED;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.FUNC_RETURN_TYPE_INVALID;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.OPERATION_CONSTRUCTOR_WRONG_PARAMETER;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.OPERATION_CONSTRUCTOR_WRONG_PARAMETER_COMBINATION;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Arrays;
import java.util.Collection;

import jakarta.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPAODataQueryContext;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class IntermediateOperationHelper {

  private IntermediateOperationHelper() {
    // Must not create instances
  }

  @SuppressWarnings("unchecked")
  static <T> Constructor<T> determineConstructor(final Method javaOperation) throws ODataJPAModelException {
    Constructor<T> result = null;
    final Constructor<T>[] constructors = (Constructor<T>[]) ((Class<T>) javaOperation.getDeclaringClass())
        .getConstructors();
    for (final Constructor<T> constructor : constructors) {
      final Parameter[] parameters = constructor.getParameters();
      if (parameters.length == 0)
        result = constructor;
      else {
        checkConstructorParameter(javaOperation, constructor);
        result = constructor;
      }
    }
    if (result == null)
      throw new ODataJPAModelException(FUNC_CONSTRUCTOR_MISSING, javaOperation.getClass().getName());
    return result;
  }

  private static <T> void checkConstructorParameter(final Method javaOperation, final Constructor<T> constructor)
      throws ODataJPAModelException {
    boolean queryContextFound = false;
    boolean otherFound = false;
    for (final Parameter p : constructor.getParameters()) {
      if (!(p.getType().isAssignableFrom(EntityManager.class)
          || p.getType().isAssignableFrom(JPARequestParameterMap.class)
          || p.getType().isAssignableFrom(JPAHttpHeaderMap.class)
          || p.getType().isAssignableFrom(JPAODataQueryContext.class)))
        throw new ODataJPAModelException(OPERATION_CONSTRUCTOR_WRONG_PARAMETER,
            javaOperation.getDeclaringClass().getName(), p.getName(), p.getType().getName());
      if (p.getType().isAssignableFrom(JPAODataQueryContext.class))
        queryContextFound = true;
      else
        otherFound = true;
    }
    if (queryContextFound && otherFound)
      throw new ODataJPAModelException(OPERATION_CONSTRUCTOR_WRONG_PARAMETER_COMBINATION,
          javaOperation.getDeclaringClass().getName());
  }

  static boolean isCollection(final Class<?> declaredReturnType) {
    for (final Class<?> inter : Arrays.asList(declaredReturnType.getInterfaces())) {
      if (inter == Collection.class)
        return true;
    }
    return false;
  }

  static FullQualifiedName determineReturnType(final ReturnType definedReturnType, final Class<?> declaredReturnType,
      final IntermediateSchema schema, final String operationName) throws ODataJPAModelException {

    final IntermediateStructuredType<?> structuredType = schema.getStructuredType(declaredReturnType);
    if (structuredType != null)
      return structuredType.getExternalFQN();
    else {
      final IntermediateEnumerationType enumType = schema.getEnumerationType(declaredReturnType);
      if (enumType != null) {
        return enumType.getExternalFQN();
      } else if (declaredReturnType.equals(Blob.class) || declaredReturnType.equals(Clob.class)) {
        // The return type '%1$s' used at method '%3$s' is not supported
        throw new ODataJPAModelException(FUNC_RETURN_NOT_SUPPORTED, declaredReturnType.getName(),
            operationName);
      } else {
        EdmPrimitiveTypeKind edmType = JPATypeConverter.convertToEdmSimpleType(declaredReturnType);
        if (edmType == null) {
          edmType = JPATypeConverter.convertToEdmSimpleType(definedReturnType.type());
          if (edmType == null)
            throw new ODataJPAModelException(FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
                declaredReturnType.getName(), operationName);
        }
        return edmType.getFullQualifiedName();
      }
    }
  }
}
