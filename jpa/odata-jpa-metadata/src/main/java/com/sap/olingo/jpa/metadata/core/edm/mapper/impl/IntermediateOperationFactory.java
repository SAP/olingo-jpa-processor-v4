package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataOperation;

public abstract class IntermediateOperationFactory {

  public IntermediateOperationFactory() {
    super();
  }

  abstract IntermediateOperation createOperation(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Method m, final Object functionDescribtion) throws ODataJPAModelException;

  Map<? extends String, ? extends IntermediateOperation> createOperationMap(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema, final Class<? extends ODataOperation> clazz,
      final Class<? extends Annotation> annotation)
      throws ODataJPAModelException {

    final Map<String, IntermediateOperation> funcList = new HashMap<String, IntermediateOperation>();
    if (reflections != null) {
      @SuppressWarnings("unchecked")
      final Set<Class<? extends ODataOperation>> operationClasses =
          (Set<Class<? extends ODataOperation>>) findJavaOperations(reflections, clazz);

      for (final Class<? extends ODataOperation> operationClass : operationClasses) {
        for (Method m : Arrays.asList(operationClass.getMethods())) {
          Object operationDescribtion = m.getAnnotation(annotation);
          if (operationDescribtion != null) {
            final IntermediateOperation func = createOperation(nameBuilder, schema, m, operationDescribtion);
            funcList.put(func.getInternalName(), createOperation(nameBuilder, schema, m, operationDescribtion));
          }
        }
      }
    }
    return funcList;
  }

  private Set<?> findJavaOperations(Reflections reflections, Class<? extends ODataOperation> clazz) {
    return reflections.getSubTypesOf(clazz);
  }

}