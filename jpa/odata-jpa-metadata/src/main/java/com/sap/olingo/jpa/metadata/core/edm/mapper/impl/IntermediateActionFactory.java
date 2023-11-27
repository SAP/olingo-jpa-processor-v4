package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataOperation;

class IntermediateActionFactory implements IntermediateOperationFactory<IntermediateJavaAction> {
//Description
  @Override
  public IntermediateJavaAction createOperation(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Method method, final Object functionDescription)
      throws ODataJPAModelException {
    return new IntermediateJavaAction(nameBuilder, (EdmAction) functionDescription, method, schema);
  }

  @SuppressWarnings("unchecked")
  <F extends IntermediateJavaAction> Map<ODataActionKey, F> create(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema) throws ODataJPAModelException {

    return (Map<ODataActionKey, F>) createActionMap(nameBuilder, reflections, schema, ODataAction.class,
        EdmAction.class);
  }

  Map<ODataActionKey, IntermediateJavaAction> createActionMap(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema, final Class<? extends ODataOperation> clazz,
      final Class<? extends Annotation> annotation)
      throws ODataJPAModelException {

    final Map<ODataActionKey, IntermediateJavaAction> operations = new HashMap<>();
    if (reflections != null) {
      final Set<?> operationClasses = findJavaOperations(reflections, clazz);

      for (final Object operationClass : operationClasses) {
        for (final Method m : Arrays.asList(((Class<?>) operationClass).getMethods())) {
          final Object operationDescription = m.getAnnotation(annotation);
          if (operationDescription != null) {
            final IntermediateOperation operation = createOperation(nameBuilder, schema, m, operationDescription);
            operations.put(new ODataActionKey(operation), (IntermediateJavaAction) operation);
          }
        }
      }
    }
    return operations;
  }

}
