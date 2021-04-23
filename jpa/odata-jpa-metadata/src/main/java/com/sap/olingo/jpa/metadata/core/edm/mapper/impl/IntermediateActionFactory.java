package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Method;
import java.util.Map;

import org.reflections8.Reflections;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.extention.ODataAction;

class IntermediateActionFactory extends IntermediateOperationFactory {

  @Override
  IntermediateOperation createOperation(JPAEdmNameBuilder nameBuilder, IntermediateSchema schema, Method m,
      Object functionDescribtion) throws ODataJPAModelException {
    return new IntermediateJavaAction(nameBuilder, (EdmAction) functionDescribtion, m, schema);
  }

  @SuppressWarnings("unchecked")
  <F extends IntermediateJavaAction> Map<String, F> create(final JPAEdmNameBuilder nameBuilder,
      final Reflections reflections, final IntermediateSchema schema) throws ODataJPAModelException {

    return (Map<String, F>) createOperationMap(nameBuilder, reflections,
        schema, ODataAction.class, EdmAction.class);
  }

}
