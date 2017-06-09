package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAJavaFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateJavaFunction extends IntermediateFunction implements JPAJavaFunction {
  private final Method javaFunction;
  private final Constructor<?> javaConstructor;
  private List<JPAFunctionParameter> parameterList;

  IntermediateJavaFunction(JPAEdmNameBuilder nameBuilder, EdmFunction jpaFunction, Method javaFunction,
      IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaFunction, schema,
        IntNameBuilder.buildFunctionName(jpaFunction).isEmpty() ? javaFunction.getName() : IntNameBuilder
            .buildFunctionName(jpaFunction));
    this.setExternalName(nameBuilder.buildOperationName(internalName));
    this.javaFunction = javaFunction;
    this.javaConstructor = determineConstructor(javaFunction);
  }

  @Override
  public EdmFunctionType getFunctionType() {
    return EdmFunctionType.JavaClass;
  }

  @Override
  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    List<CsdlParameter> parameterList = new ArrayList<CsdlParameter>();
    for (Parameter declairedParameter : Arrays.asList(javaFunction.getParameters())) {
      CsdlParameter parameter = new CsdlParameter();
      EdmFunctionParameter definedParameter = declairedParameter.getAnnotation(EdmFunctionParameter.class);
      parameter.setName(nameBuilder.buildPropertyName(definedParameter.name()));
      EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedParameter.getType());
      if (edmType == null)
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_PARAM_ONLY_PRIMITIVE, javaFunction
            .getDeclaringClass().getName(), javaFunction.getName(), definedParameter.name());
      parameter.setType(edmType.getFullQualifiedName());
      parameterList.add(parameter);
    }
    return parameterList;
  }

  // TODO handle multiple schemas
  @Override
  protected CsdlReturnType determineEdmResultType(final ReturnType definedReturnType) throws ODataJPAModelException {
    final CsdlReturnType edmResultType = new CsdlReturnType();
    Class<?> declairedReturnType = javaFunction.getReturnType();

    if (isCollection(declairedReturnType)) {
      if (definedReturnType.type() == Object.class)
        // Type parameter expected for %1$s
        throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_EXP, javaFunction.getName());
      edmResultType.setCollection(true);
      edmResultType.setType(JPATypeConvertor.convertToEdmSimpleType(definedReturnType.type()).getFullQualifiedName());
    } else {
      if (definedReturnType.type() != Object.class
          && !definedReturnType.type().getCanonicalName().equals(declairedReturnType.getCanonicalName()))
        // The return type %1$s from EdmFunction does not match type %2$s declared at method %3$s
        throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
            declairedReturnType.getName(), javaFunction.getName());

      edmResultType.setCollection(false);
      IntermediateStructuredType structuredType = schema.getStructuredType(declairedReturnType);
      if (structuredType != null)
        edmResultType.setType(structuredType.getExternalFQN());
      else {
        final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedReturnType);
        if (edmType == null)
          throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
              declairedReturnType.getName(), javaFunction.getName());
        edmResultType.setType(edmType.getFullQualifiedName());
      }
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
  boolean hasFunctionImport() {
    return true;
  }

  private Constructor<?> determineConstructor(Method javaFunction) throws ODataJPAModelException {
    Constructor<?> result = null;
    Constructor<?>[] constructors = javaFunction.getDeclaringClass().getConstructors();
    for (Constructor<?> constructor : Arrays.asList(constructors)) {
      Parameter[] parameters = constructor.getParameters();
      if (parameters.length == 0)
        result = constructor;
      else if (parameters.length == 1 && parameters[0].getType() == EntityManager.class) {
        result = constructor;
        break;
      }
    }
    if (result == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_CONSTRUCTOR_MISSING, javaFunction
          .getClass().getName());
    return result;
  }

  private boolean isCollection(Class<?> declairedReturnType) {
    for (Class<?> inter : Arrays.asList(declairedReturnType.getInterfaces())) {
      if (inter == Collection.class)
        return true;
    }
    return false;
  }

  @Override
  public Method getMethod() {
    return javaFunction;
  }

  @Override
  public Constructor<?> getConstructor() {
    return javaConstructor;
  }

  @Override
  public List<JPAFunctionParameter> getParameter() throws ODataJPAModelException {
    if (parameterList == null) {
      parameterList = new ArrayList<JPAFunctionParameter>();
      Class<?>[] types = javaFunction.getParameterTypes();
      Parameter[] declairedParameters = javaFunction.getParameters();
      for (int i = 0; i < declairedParameters.length; i++) {
        Parameter declairedParameter = declairedParameters[i];
        EdmFunctionParameter definedParameter = declairedParameter.getAnnotation(EdmFunctionParameter.class);
        if (definedParameter == null)
          // Function parameter %1$s of method %2$s at class %3$s without required annotation
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_PARAM_ANNOTATION_MISSING,
              declairedParameter.getName(), javaFunction.getName(), javaFunction
                  .getDeclaringClass().getName());
        JPAFunctionParameter parameter = new IntermediatFunctionParameter(definedParameter, nameBuilder
            .buildPropertyName(definedParameter.name()), declairedParameter.getName(), types[i]);
        parameterList.add(parameter);
      }

    }
    return parameterList;
  }

  @Override
  public JPAFunctionParameter getParameter(String internalName) throws ODataJPAModelException {
    for (JPAFunctionParameter parameter : getParameter()) {
      if (parameter.getInternalName() == internalName)
        return parameter;
    }
    return null;
  }
}
