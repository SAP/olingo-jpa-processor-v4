package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataOperation;

/**
 *
 * @author Oliver Grande
 *
 * @param <O> Type of operation
 */
public abstract class IntermediateOperationFactory<O extends IntermediateOperation> {

  IntermediateOperationFactory() {
    super();
  }

  abstract O createOperation(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Method method, final Object functionDescription)
      throws ODataJPAModelException;

  @SuppressWarnings("unchecked")
  Map<String, O> createOperationMap(final JPAEdmNameBuilder nameBuilder, final Reflections reflections,
      final IntermediateSchema schema, final Class<? extends ODataOperation> clazz,
      final Class<? extends Annotation> annotation)
      throws ODataJPAModelException {

    final Map<String, O> operations = new HashMap<>();
    if (reflections != null) {
      final Set<?> operationClasses = findJavaOperations(reflections, clazz);
      for (final Object operationClass : operationClasses) {
        processOneClass(nameBuilder, schema, annotation, operations, (Class<? extends ODataOperation>) operationClass);
      }
    }
    return operations;
  }

  <T extends ODataOperation> Set<Class<? extends T>> findJavaOperations(final Reflections reflections,
      final Class<T> clazz) {
    return reflections.getSubTypesOf(clazz);
  }

  private void processOneClass(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Class<? extends Annotation> annotation, final Map<String, O> operations,
      final Class<? extends ODataOperation> operationClass)
      throws ODataJPAModelException {

    for (final Method m : Arrays.asList(operationClass.getMethods())) {
      final Object operationDescription = m.getAnnotation(annotation);
      if (operationDescription != null) {
        final O operation = createOperation(nameBuilder, schema, m, operationDescription);
        operations.put(operation.getInternalName(), operation);
      }
    }
  }

}