package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateJavaFunction extends IntermediateFunction implements JPAJavaFunction {
  private final Method javaFunction;
  private final Constructor<?> javaConstructor;
  private List<JPAParameter> parameterList;

  IntermediateJavaFunction(JPAEdmNameBuilder nameBuilder, EdmFunction jpaFunction, Method javaFunction,
      IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaFunction, schema,
        IntNameBuilder.buildFunctionName(jpaFunction).isEmpty() ? javaFunction.getName() : IntNameBuilder
            .buildFunctionName(jpaFunction));
    this.setExternalName(nameBuilder.buildOperationName(internalName));
    this.javaFunction = javaFunction;
    this.javaConstructor = IntermediateOperationHelper.determineConstructor(javaFunction);
  }

  @Override
  public Constructor<?> getConstructor() {
    return javaConstructor;
  }

  @Override
  public EdmFunctionType getFunctionType() {
    return EdmFunctionType.JavaClass;
  }

  @Override
  public Method getMethod() {
    return javaFunction;
  }

  @Override
  public List<JPAParameter> getParameter() throws ODataJPAModelException {
    if (parameterList == null) {
      parameterList = new ArrayList<>();
      Class<?>[] types = javaFunction.getParameterTypes();
      Parameter[] declairedParameters = javaFunction.getParameters();
      for (int i = 0; i < declairedParameters.length; i++) {
        Parameter declairedParameter = declairedParameters[i];
        EdmParameter definedParameter = declairedParameter.getAnnotation(EdmParameter.class);
        if (definedParameter == null)
          // Function parameter %1$s of method %2$s at class %3$s without required annotation
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_PARAM_ANNOTATION_MISSING,
              declairedParameter.getName(), javaFunction.getName(), javaFunction
                  .getDeclaringClass().getName());
        JPAParameter parameter = new IntermediatFunctionParameter(definedParameter, nameBuilder
            .buildPropertyName(definedParameter.name()), declairedParameter.getName(), types[i]);
        parameterList.add(parameter);
      }

    }
    return parameterList;
  }

  @Override
  public JPAParameter getParameter(String internalName) throws ODataJPAModelException {
    for (JPAParameter parameter : getParameter()) {
      if (parameter.getInternalName() == internalName)
        return parameter;
    }
    return null;
  }

  @Override
  public JPAOperationResultParameter getResultParameter() {
    return new IntermediatOperationResultParameter(this, jpaFunction.returnType(), javaFunction.getReturnType(),
        IntermediateOperationHelper.isCollection(javaFunction.getReturnType()));
  }

  @Override
  public CsdlReturnType getReturnType() {
    return edmFunction.getReturnType();
  }

  @Override
  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    List<CsdlParameter> parameters = new ArrayList<>();
    for (Parameter declairedParameter : Arrays.asList(javaFunction.getParameters())) {
      CsdlParameter parameter = new CsdlParameter();
      EdmParameter definedParameter = declairedParameter.getAnnotation(EdmParameter.class);
      parameter.setName(nameBuilder.buildPropertyName(definedParameter.name()));
      EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedParameter.getType());
      if (edmType == null)
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_PARAM_ONLY_PRIMITIVE, javaFunction
            .getDeclaringClass().getName(), javaFunction.getName(), definedParameter.name());
      parameter.setType(edmType.getFullQualifiedName());
      parameters.add(parameter);
    }
    return parameters;
  }

  // TODO handle multiple schemas
  @Override
  protected CsdlReturnType determineEdmResultType(final ReturnType definedReturnType) throws ODataJPAModelException {
    final CsdlReturnType edmResultType = new CsdlReturnType();
    Class<?> declairedReturnType = javaFunction.getReturnType();

    if (IntermediateOperationHelper.isCollection(declairedReturnType)) {
      if (definedReturnType.type() == Object.class)
        // Type parameter expected for %1$s
        throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_EXP, javaFunction.getName());
      edmResultType.setCollection(true);
      edmResultType.setType(IntermediateOperationHelper.determineReturnType(definedReturnType, definedReturnType.type(),
          schema, javaFunction.getName()));
    } else {
      if (definedReturnType.type() != Object.class
          && !definedReturnType.type().getCanonicalName().equals(declairedReturnType.getCanonicalName()))
        // The return type %1$s from EdmFunction does not match type %2$s declared at method %3$s
        throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
            declairedReturnType.getName(), javaFunction.getName());

      edmResultType.setCollection(false);
      edmResultType.setType(IntermediateOperationHelper.determineReturnType(definedReturnType, declairedReturnType,
          schema, javaFunction.getName()));
    }

    edmResultType.setNullable(definedReturnType.isNullable());
    if (definedReturnType.maxLength() >= 0)
      edmResultType.setMaxLength(definedReturnType.maxLength());
    if (definedReturnType.precision() >= 0)
      edmResultType.setPrecision(definedReturnType.precision());
    if (definedReturnType.scale() >= 0)
      edmResultType.setScale(definedReturnType.scale());
    if (definedReturnType.srid() != null && !definedReturnType.srid().srid().isEmpty()) {
      final SRID srid = SRID.valueOf(definedReturnType.srid().srid());
      srid.setDimension(definedReturnType.srid().dimension());
      edmResultType.setSrid(srid);
    }

    return edmResultType;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    super.lazyBuildEdmItem();
    edmFunction.setBound(false);
  }

  @Override
  boolean hasImport() {
    return true;
  }

//  private FullQualifiedName determineReturnType(final ReturnType definedReturnType, final Class<?> declairedReturnType)
//      throws ODataJPAModelException {
//
//    IntermediateStructuredType structuredType = schema.getStructuredType(declairedReturnType);
//    if (structuredType != null)
//      return structuredType.getExternalFQN();
//    else {
//      final IntermediateEnumerationType enumType = schema.getEnumerationType(declairedReturnType);
//      if (enumType != null) {
//        return enumType.getExternalFQN();
//      } else {
//        final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedReturnType);
//        if (edmType == null)
//          throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
//              declairedReturnType.getName(), javaFunction.getName());
//        return edmType.getFullQualifiedName();
//      }
//    }
//  }
}
