package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

public class IntermediateJavaAction extends IntermediateModelElement {

  private CsdlAction edmAction;
  private final EdmAction jpaAction;
  private final IntermediateSchema schema;
  private final Method javaAction;
  private final Constructor<?> javaConstructor;
  private List<JPAParameter> parameterList;

  IntermediateJavaAction(JPAEdmNameBuilder nameBuilder, EdmAction jpaAction, Method javaAction,
      IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, IntNameBuilder.buildActionName(jpaAction).isEmpty() ? javaAction.getName() : IntNameBuilder
        .buildActionName(jpaAction));

    this.schema = schema;
    this.jpaAction = jpaAction;
    this.setExternalName(nameBuilder.buildOperationName(internalName));
    this.javaAction = javaAction;
    this.javaConstructor = IntermediateOperationHelper.determineConstructor(javaAction);
  }

  public Constructor<?> getConstructor() {
    return javaConstructor;
  }

  public List<JPAParameter> getParameter() throws ODataJPAModelException {
    if (parameterList == null) {
      parameterList = new ArrayList<JPAParameter>();
      Class<?>[] types = javaAction.getParameterTypes();
      Parameter[] declairedParameters = javaAction.getParameters();
      for (int i = 0; i < declairedParameters.length; i++) {
        Parameter declairedParameter = declairedParameters[i];
        EdmParameter definedParameter = declairedParameter.getAnnotation(EdmParameter.class);
        if (definedParameter == null)
          // Function parameter %1$s of method %2$s at class %3$s without required annotation
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_PARAM_ANNOTATION_MISSING,
              declairedParameter.getName(), javaAction.getName(), javaAction
                  .getDeclaringClass().getName());
        JPAParameter parameter = new IntermediateOperationParameter(
            nameBuilder,
            definedParameter,
            nameBuilder.buildPropertyName(definedParameter.name()),
            declairedParameter.getName(),
            types[i]);
        parameterList.add(parameter);
      }

    }
    return parameterList;
  }

  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    final List<CsdlParameter> parameterList = new ArrayList<CsdlParameter>();
    final List<JPAParameter> jpaParameterList = getParameter();
    int bindingPosition = 0;
    for (int i = 0; i < jpaParameterList.size(); i++) {
      final JPAParameter jpaParameter = jpaParameterList.get(i);
      final CsdlParameter parameter = new CsdlParameter();
      parameter.setName(jpaParameter.getName());
      final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(jpaParameter.getType());
      if (!jpaAction.isBound()) {
        if (edmType == null)
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_PARAM_ONLY_PRIMITIVE,
              javaAction.getDeclaringClass().getName(), javaAction.getName(), jpaParameter.getInternalName());

        parameter.setType(edmType.getFullQualifiedName());
      } else if (edmType == null) {
        final IntermediateStructuredType structuredType = schema.getEntityType(jpaParameter.getType());
        if (structuredType != null) {
          if (bindingPosition == 0)
            bindingPosition = i + 1;
          parameter.setType(structuredType.getExternalFQN());
        } else
          // The type of %1$s of action of method %2$s of class %1$s could not be converted
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_PARAM_ONLY_PRIMITIVE,
              jpaParameter.getInternalName(), javaAction.getName(), javaAction.getDeclaringClass().getName());
      }
      parameter.setPrecision(jpaParameter.getPrecision());
      parameter.setScale(jpaParameter.getScale());
      parameter.setMaxLength(jpaParameter.getMaxLength());
      parameter.setSrid(jpaParameter.getSrid());
      parameterList.add(parameter);
    }
    if (jpaAction.isBound() && bindingPosition != 1)
      // Binding parameter not found within in interface of method %1$s of class %2$s. Binding parameter must be the
      // first parameter.
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_PARAM_BINGING_NOT_FOUND,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    return parameterList;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmAction == null) {
      edmAction = new CsdlAction();
//      edmAction.setAnnotations(annotations);
      edmAction.setBound(jpaAction.isBound());
      edmAction.setName(getExternalName());
      edmAction.setParameters(returnNullIfEmpty(determineEdmInputParameter()));
      edmAction.setReturnType(determineEdmResultType(jpaAction.returnType(), javaAction));
      edmAction.setEntitySetPath(setEntitySetPath());
    }
  }

  private String setEntitySetPath() throws ODataJPAModelException {
    if (jpaAction.entitySetPath() == null || jpaAction.entitySetPath().isEmpty())
      return null;
    if (!jpaAction.isBound())
      // Entity Set Path shall only provided for bound actions. Action method %1$s of class %2$s is unbound.
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_UNBOUND_ENTITY_SET,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    if (!edmAction.getReturnType().isCollection() && schema.getEntityType(javaAction.getReturnType()) == null)
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_UNBOUND_ENTITY_SET,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    return jpaAction.entitySetPath();
  }

  @Override
  CsdlAction getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmAction;
  }

  boolean hasActionImport() {
    return !jpaAction.isBound();
  }

  boolean isBound() throws ODataJPAModelException {
    return getEdmItem().isBound();
  }

  private CsdlReturnType determineEdmResultType(final ReturnType definedReturnType, final Method javaOperation)
      throws ODataJPAModelException {
    final CsdlReturnType edmResultType = new CsdlReturnType();
    final Class<?> declairedReturnType = javaOperation.getReturnType();

    if (declairedReturnType == void.class)
      return null;

    if (IntermediateOperationHelper.isCollection(declairedReturnType)) {
      if (definedReturnType.type() == Object.class)
        // Type parameter expected for %1$s
        throw new ODataJPAModelException(MessageKeys.ACTION_RETURN_TYPE_EXP, javaOperation.getName(), javaOperation
            .getName());
      edmResultType.setCollection(true);
      edmResultType.setType(determineReturnType(definedReturnType, definedReturnType.type(), javaOperation));
    } else {
      edmResultType.setCollection(false);
      edmResultType.setType(determineReturnType(definedReturnType, declairedReturnType, javaOperation));
    }
    edmResultType.setNullable(definedReturnType.isNullable());
    edmResultType.setPrecision(definedReturnType.precision());
    edmResultType.setScale(definedReturnType.scale());
    edmResultType.setMaxLength(definedReturnType.maxLength());
    if (definedReturnType.srid() != null && !definedReturnType.srid().srid().isEmpty()) {
      final SRID srid = SRID.valueOf(definedReturnType.srid().srid());
      srid.setDimension(definedReturnType.srid().dimension());
      edmResultType.setSrid(srid);
    }
    return edmResultType;
  }

  private FullQualifiedName determineReturnType(final ReturnType definedReturnType, final Class<?> declairedReturnType,
      final Method javaOperation) throws ODataJPAModelException {

    final IntermediateStructuredType structuredType = schema.getStructuredType(declairedReturnType);
    if (structuredType != null)
      return structuredType.getExternalFQN();
    else {
      final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(declairedReturnType);
      if (edmType == null)
        throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_INVALID, definedReturnType.type().getName(),
            declairedReturnType.getName(), javaOperation.getName());
      return edmType.getFullQualifiedName();
    }
  }
}
