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
      final Method m, final Object functionDescription) throws ODataJPAModelException;

  Map<String, O> createOperationMap(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema, final Class<? extends ODataOperation> clazz,
      final Class<? extends Annotation> annotation)
      throws ODataJPAModelException {

    final Map<String, O> funcList = new HashMap<>();
    if (reflections != null) {
      @SuppressWarnings("unchecked")
      final Set<Class<? extends ODataOperation>> operationClasses =
          (Set<Class<? extends ODataOperation>>) findJavaOperations(reflections, clazz);

      for (final Class<? extends ODataOperation> operationClass : operationClasses) {
        processOneClass(nameBuilder, schema, annotation, funcList, operationClass);
      }
    }
    return funcList;
  }

  private Set<?> findJavaOperations(final Reflections reflections, final Class<? extends ODataOperation> clazz) {
    return reflections.getSubTypesOf(clazz);
  }

  private void processOneClass(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Class<? extends Annotation> annotation, final Map<String, O> funcList,
      final Class<? extends ODataOperation> operationClass) throws ODataJPAModelException {

    for (final Method m : Arrays.asList(operationClass.getMethods())) {
      final Object operationDescription = m.getAnnotation(annotation);
      if (operationDescription != null) {
        final IntermediateOperation func = createOperation(nameBuilder, schema, m, operationDescription);
        funcList.put(func.getInternalName(), createOperation(nameBuilder, schema, m, operationDescription));
      }
    }
  }

}