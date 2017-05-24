package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateJavaFunction extends IntermediateFunction {
  private final Method javaFunction;

  IntermediateJavaFunction(JPAEdmNameBuilder nameBuilder, EdmFunction jpaFunction, Method javaFunction,
      IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaFunction, schema,
        IntNameBuilder.buildFunctionName(jpaFunction).isEmpty() ? javaFunction.getName() : IntNameBuilder
            .buildFunctionName(jpaFunction));
    this.javaFunction = javaFunction;
  }

  // TODO handle multiple schemas
  @Override
  protected CsdlReturnType determineEdmResultType(final ReturnType returnType) throws ODataJPAModelException {
    return new CsdlReturnType();
  }

  @Override
  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    return new ArrayList<CsdlParameter>();
  }
}
