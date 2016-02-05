package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.metamodel.EntityType;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateFunctionFactory {

  Map<? extends String, ? extends IntermediateFunction> create(JPAEdmNameBuilder nameBuilder,
      EntityType<?> jpaEntityType,
      IntermediateSchema schema) throws ODataJPAModelException {
    HashMap<String, IntermediateFunction> funcList = new HashMap<String, IntermediateFunction>();

    if (jpaEntityType.getJavaType() instanceof AnnotatedElement) {
      EdmFunctions jpaStoredProcedureList = ((AnnotatedElement) jpaEntityType.getJavaType())
          .getAnnotation(EdmFunctions.class);
      if (jpaStoredProcedureList != null) {
        for (EdmFunction jpaStoredProcedure : jpaStoredProcedureList.value()) {
          putFunction(nameBuilder, jpaEntityType, schema, funcList, jpaStoredProcedure);
        }
      } else {
        EdmFunction jpaStoredProcedure = ((AnnotatedElement) jpaEntityType.getJavaType())
            .getAnnotation(EdmFunction.class);
        if (jpaStoredProcedure != null)
          putFunction(nameBuilder, jpaEntityType, schema, funcList, jpaStoredProcedure);
      }
    }
    return funcList;
  }

  private void putFunction(JPAEdmNameBuilder nameBuilder, EntityType<?> jpaEntityType, IntermediateSchema schema,
      HashMap<String, IntermediateFunction> funcList, EdmFunction jpaStoredProcedure) throws ODataJPAModelException {
    IntermediateFunction func = new IntermediateFunction(nameBuilder, jpaStoredProcedure, jpaEntityType
        .getJavaType(), schema);
    funcList.put(func.getInternalName(), func);
  }

}
