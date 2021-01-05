package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Method;
import java.util.Map;

import org.reflections.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extension.ODataAction;

class IntermediateActionFactory extends IntermediateOperationFactory<IntermediateJavaAction> {
//Description
  @Override
  IntermediateJavaAction createOperation(final JPAEdmNameBuilder nameBuilder, final IntermediateSchema schema,
      final Method m,
      final Object functionDescription) throws ODataJPAModelException {
    return new IntermediateJavaAction(nameBuilder, (EdmAction) functionDescription, m, schema);
  }

  @SuppressWarnings("unchecked")
  <F extends IntermediateJavaAction> Map<String, F> create(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema) throws ODataJPAModelException {

    return (Map<String, F>) createOperationMap(nameBuilder, reflections,
        schema, ODataAction.class, EdmAction.class);
  }

}
