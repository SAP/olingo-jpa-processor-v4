package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.metamodel.EntityType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateFunctionFactory {

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

  private void putFunction(final JPAEdmNameBuilder nameBuilder, final EntityType<?> jpaEntityType,
      final IntermediateSchema schema,
      final Map<String, IntermediateFunction> funcList, final EdmFunction jpaStoredProcedure)
          throws ODataJPAModelException {
    final IntermediateFunction func = new IntermediateFunction(nameBuilder, jpaStoredProcedure, jpaEntityType
        .getJavaType(), schema);
    funcList.put(func.getInternalName(), func);
  }

}
