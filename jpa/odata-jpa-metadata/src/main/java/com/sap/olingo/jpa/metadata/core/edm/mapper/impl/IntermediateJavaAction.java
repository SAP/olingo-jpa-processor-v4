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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateJavaAction extends IntermediateOperation implements JPAAction {

  private CsdlAction edmAction;
  final EdmAction jpaAction;
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

  @Override
  public Constructor<?> getConstructor() {
    return javaConstructor;
  }

  @Override
  public Method getMethod() {
    return javaAction;
  }

  public List<JPAParameter> getParameter() throws ODataJPAModelException {
    if (parameterList == null) {
      parameterList = new ArrayList<>();
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

  @Override
  public JPAParameter getParameter(Parameter declairedParameter) throws ODataJPAModelException {
    for (JPAParameter param : getParameter()) {
      if (param.getInternalName().equals(declairedParameter.getName()))
        return param;
    }
    return null;
  }

  @Override
  public JPAOperationResultParameter getResultParameter() {
    return new IntermediatOperationResultParameter(this, jpaAction.returnType(), javaAction.getReturnType(),
        IntermediateOperationHelper.isCollection(javaAction.getReturnType()));
  }

  @Override
  public CsdlReturnType getReturnType() {
    return edmAction.getReturnType();
  }

  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    final List<CsdlParameter> parameters = new ArrayList<>();
    final List<JPAParameter> jpaParameterList = getParameter();
    final BindingPosition bindingPosition = new BindingPosition();

    for (int i = 0; i < jpaParameterList.size(); i++) {
      final JPAParameter jpaParameter = jpaParameterList.get(i);
      final CsdlParameter parameter = new CsdlParameter();
      parameter.setName(jpaParameter.getName());
      parameter.setType(determineParameterType(bindingPosition, i, jpaParameter));
      parameter.setPrecision(nullIfNotSet(jpaParameter.getPrecision()));
      parameter.setScale(nullIfNotSet(jpaParameter.getScale()));
      parameter.setMaxLength(nullIfNotSet(jpaParameter.getMaxLength()));
      parameter.setSrid(jpaParameter.getSrid());
      parameters.add(parameter);
    }
    if (jpaAction.isBound() && bindingPosition.getPos() != 1)
      // Binding parameter not found within in the interface of method %1$s of class %2$s. Binding parameter must be the
      // first parameter.
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_PARAM_BINDING_NOT_FOUND,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    return parameters;
  }

  private FullQualifiedName determineParameterType(final BindingPosition bindingPosition, final int i,
      final JPAParameter jpaParameter) throws ODataJPAModelException {

    final EdmPrimitiveTypeKind edmType = JPATypeConvertor.convertToEdmSimpleType(jpaParameter.getType());
    if (edmType != null)
      return edmType.getFullQualifiedName();
    final IntermediateEnumerationType enumType = schema.getEnumerationType(jpaParameter.getType());
    if (enumType != null) {
      return enumType.getExternalFQN();
    } else {
      final IntermediateStructuredType structuredType = schema.getEntityType(jpaParameter.getType());
      if (structuredType != null) {
        if (bindingPosition.getPos() == 0)
          bindingPosition.setPos(i + 1);
        return structuredType.getExternalFQN();
      } else
        // The type of %1$s of action of method %2$s of class %1$s could not be converted
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ACTION_PARAM_ONLY_PRIMITIVE,
            jpaParameter.getInternalName(), javaAction.getName(), javaAction.getDeclaringClass().getName());
    }
  }

  @Override
  protected boolean hasImport() {
    return !jpaAction.isBound();
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

  @Override
  CsdlAction getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmAction;
  }

  @Override
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
      edmResultType.setType(IntermediateOperationHelper.determineReturnType(definedReturnType, definedReturnType.type(),
          schema, javaOperation.getName()));
    } else {
      edmResultType.setCollection(false);
      edmResultType.setType(IntermediateOperationHelper.determineReturnType(definedReturnType, declairedReturnType,
          schema, javaOperation.getName()));
    }
    edmResultType.setNullable(definedReturnType.isNullable());
    edmResultType.setPrecision(nullIfNotSet(definedReturnType.precision()));
    edmResultType.setScale(nullIfNotSet(definedReturnType.scale()));
    edmResultType.setMaxLength(nullIfNotSet(definedReturnType.maxLength()));
    if (definedReturnType.srid() != null && !definedReturnType.srid().srid().isEmpty()) {
      final SRID srid = SRID.valueOf(definedReturnType.srid().srid());
      srid.setDimension(definedReturnType.srid().dimension());
      edmResultType.setSrid(srid);
    }
    return edmResultType;
  }

  private Integer nullIfNotSet(Integer number) {
    if (number != null && number > -1)
      return number;
    return null;
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

  private static class BindingPosition {
    private Integer pos = 0;

    Integer getPos() {
      return pos;
    }

    void setPos(Integer pos) {
      this.pos = pos;
    }

  }
}
