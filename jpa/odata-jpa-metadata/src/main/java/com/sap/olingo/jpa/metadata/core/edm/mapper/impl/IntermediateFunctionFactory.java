package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataFunction;

import jakarta.persistence.metamodel.EntityType;

final class IntermediateFunctionFactory<F extends IntermediateFunction> implements IntermediateOperationFactory<F> {

  /**
   * Creates all functions declared at on entity type
   * @param nameBuilder
   * @param jpaEntityType
   * @param schema
   * @return
   * @throws ODataJPAModelException
   */
  Map<String, F> create(final JPAEdmNameBuilder nameBuilder,
      final EntityType<?> jpaEntityType, final IntermediateSchema schema) {

    final Map<String, F> functionList = new HashMap<>();

    if (jpaEntityType.getJavaType() instanceof AnnotatedElement) {
      final EdmFunctions jpaStoredProcedureList = jpaEntityType.getJavaType()
          .getAnnotation(EdmFunctions.class);
      if (jpaStoredProcedureList != null) {
        for (final EdmFunction jpaStoredProcedure : jpaStoredProcedureList.value()) {
          putFunction(nameBuilder, jpaEntityType, schema, functionList, jpaStoredProcedure);
        }
      } else {
        final EdmFunction jpaStoredProcedure = jpaEntityType.getJavaType()
            .getAnnotation(EdmFunction.class);
        if (jpaStoredProcedure != null)
          putFunction(nameBuilder, jpaEntityType, schema, functionList, jpaStoredProcedure);
      }
    }
    return functionList;
  }

  Map<String, F> create(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema) throws ODataJPAModelException {
    return createOperationMap(nameBuilder, reflections, schema,
        ODataFunction.class, EdmFunction.class);
  }

  @SuppressWarnings("unchecked")
  @Override
  public F createOperation(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Method method, final Object functionDescription)
      throws ODataJPAModelException {
    return (F) new IntermediateJavaFunction(nameBuilder, (EdmFunction) functionDescription, method, schema);
  }

  @SuppressWarnings("unchecked")
  private void putFunction(final JPAEdmNameBuilder nameBuilder, final EntityType<?> jpaEntityType,
      final IntermediateSchema schema, final Map<String, F> functionList,
      final EdmFunction jpaStoredProcedure) {

    final IntermediateFunction function = new IntermediateDataBaseFunction(nameBuilder, jpaStoredProcedure,
        jpaEntityType.getJavaType(), schema);
    functionList.put(function.getInternalName(), (F) function);
  }

}
