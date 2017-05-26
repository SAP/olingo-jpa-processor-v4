package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.persistence.metamodel.EntityType;

import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataFunction;

final class IntermediateFunctionFactory {

  private Set<Class<? extends ODataFunction>> findJavaFunctions(Reflections reflections) {
    return reflections.getSubTypesOf(ODataFunction.class);
  }

  /**
   * Creates all functions declared at on entity type
   * @param nameBuilder
   * @param jpaEntityType
   * @param schema
   * @return
   * @throws ODataJPAModelException
   */
  Map<? extends String, ? extends IntermediateFunction> create(final JPAEdmNameBuilder nameBuilder,
      final EntityType<?> jpaEntityType, final IntermediateSchema schema) throws ODataJPAModelException {

    final Map<String, IntermediateFunction> funcList = new HashMap<String, IntermediateFunction>();

    if (jpaEntityType.getJavaType() instanceof AnnotatedElement) {
      final EdmFunctions jpaStoredProcedureList = ((AnnotatedElement) jpaEntityType.getJavaType())
          .getAnnotation(EdmFunctions.class);
      if (jpaStoredProcedureList != null) {
        for (final EdmFunction jpaStoredProcedure : jpaStoredProcedureList.value()) {
          putFunction(nameBuilder, jpaEntityType, schema, funcList, jpaStoredProcedure);
        }
      } else {
        final EdmFunction jpaStoredProcedure = ((AnnotatedElement) jpaEntityType.getJavaType())
            .getAnnotation(EdmFunction.class);
        if (jpaStoredProcedure != null)
          putFunction(nameBuilder, jpaEntityType, schema, funcList, jpaStoredProcedure);
      }
    }
    return funcList;
  }

  Map<? extends String, ? extends IntermediateFunction> create(JPAEdmNameBuilder nameBuilder,
      Reflections reflections, IntermediateSchema schema) throws ODataJPAModelException {

    final Map<String, IntermediateFunction> funcList = new HashMap<String, IntermediateFunction>();
    if (reflections != null) {
      final Set<Class<? extends ODataFunction>> functionClasses = findJavaFunctions(reflections);

      for (final Class<? extends ODataFunction> functionClass : functionClasses) {
        for (Method m : Arrays.asList(functionClass.getMethods())) {
          EdmFunction functionDescribtion = m.getAnnotation(EdmFunction.class);
          if (functionDescribtion != null) {
            final IntermediateFunction func = new IntermediateJavaFunction(nameBuilder, functionDescribtion, m, schema);
            funcList.put(func.getInternalName(), func);
          }
        }
      }
    }
    return funcList;
  }

  private void putFunction(final JPAEdmNameBuilder nameBuilder, final EntityType<?> jpaEntityType,
      final IntermediateSchema schema, final Map<String, IntermediateFunction> funcList,
      final EdmFunction jpaStoredProcedure) throws ODataJPAModelException {

    final IntermediateFunction func = new IntermediateDataBaseFunction(nameBuilder, jpaStoredProcedure, jpaEntityType
        .getJavaType(), schema);
    funcList.put(func.getInternalName(), func);
  }

}
