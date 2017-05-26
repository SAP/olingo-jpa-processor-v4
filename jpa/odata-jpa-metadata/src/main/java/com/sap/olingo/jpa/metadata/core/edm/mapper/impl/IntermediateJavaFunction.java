package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateJavaFunction extends IntermediateFunction {
  private final Method javaFunction;

  IntermediateJavaFunction(JPAEdmNameBuilder nameBuilder, EdmFunction jpaFunction, Method javaFunction,
      IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaFunction, schema,
        IntNameBuilder.buildFunctionName(jpaFunction).isEmpty() ? javaFunction.getName() : IntNameBuilder
            .buildFunctionName(jpaFunction));
    this.setExternalName(nameBuilder.buildOperationName(internalName));
    this.javaFunction = javaFunction;
  }

  // TODO handle multiple schemas
  @Override
  protected CsdlReturnType determineEdmResultType(final ReturnType definedReturnType) throws ODataJPAModelException {
    CsdlReturnType returnType = new CsdlReturnType();
    Class<?> declairedReturnType = javaFunction.getReturnType();

    returnType.setType(JPATypeConvertor.convertToEdmSimpleType(declairedReturnType).getFullQualifiedName());
    return returnType;
  }

  @Override
  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    List<CsdlParameter> parameterList = new ArrayList<CsdlParameter>();
    for (Parameter declairedParameter : Arrays.asList(javaFunction.getParameters())) {
      CsdlParameter parameter = new CsdlParameter();
      EdmFunctionParameter definedParameter = declairedParameter.getAnnotation(EdmFunctionParameter.class);
      parameter.setName(nameBuilder.buildPropertyName(definedParameter.name()));
      parameter.setType(JPATypeConvertor.convertToEdmSimpleType(declairedParameter.getType()).getFullQualifiedName());
      parameterList.add(parameter);
    }
    return parameterList;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    super.lazyBuildEdmItem();
    edmFunction.setBound(false);
  }
}
