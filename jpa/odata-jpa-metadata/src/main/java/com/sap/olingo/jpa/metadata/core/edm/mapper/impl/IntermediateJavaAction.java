package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ACTION_PARAM_ANNOTATION_MISSING;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ACTION_PARAM_BINDING_NOT_FOUND;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ACTION_PARAM_ONLY_PRIMITIVE;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ACTION_RETURN_TYPE_EXP;
import static com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys.ACTION_UNBOUND_ENTITY_SET;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAction;
import org.apache.olingo.commons.api.edm.provider.CsdlMapping;
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

class IntermediateJavaAction extends IntermediateOperation implements JPAAction {

  private CsdlAction edmAction;
  final EdmAction jpaAction;
  private final IntermediateSchema schema;
  private final Method javaAction;
  private final Constructor<?> javaConstructor;
  private List<JPAParameter> parameterList;

  IntermediateJavaAction(final JPAEdmNameBuilder nameBuilder, final EdmAction jpaAction, final Method javaAction,
      final IntermediateSchema schema) throws ODataJPAModelException {

    super(nameBuilder, InternalNameBuilder.buildActionName(jpaAction).isEmpty() ? javaAction.getName()
        : InternalNameBuilder.buildActionName(jpaAction), schema.getAnnotationInformation());

    this.schema = schema;
    this.jpaAction = jpaAction;
    this.setExternalName(nameBuilder.buildOperationName(internalName));
    this.javaAction = javaAction;
    this.javaConstructor = IntermediateOperationHelper.determineConstructor(javaAction);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Constructor<T> getConstructor() {
    return (Constructor<T>) javaConstructor;
  }

  @Override
  public Method getMethod() {
    return javaAction;
  }

  public List<JPAParameter> getParameter() throws ODataJPAModelException {
    if (parameterList == null) {
      parameterList = new ArrayList<>();
      final Class<?>[] types = javaAction.getParameterTypes();
      final Parameter[] declaredParameters = javaAction.getParameters();
      for (int i = 0; i < declaredParameters.length; i++) {
        final Parameter declaredParameter = declaredParameters[i];
        final EdmParameter definedParameter = declaredParameter.getAnnotation(EdmParameter.class);
        if (definedParameter == null)
          // Function parameter %1$s of method %2$s at class %3$s without required annotation
          throw new ODataJPAModelException(ACTION_PARAM_ANNOTATION_MISSING,
              declaredParameter.getName(), javaAction.getName(), javaAction
                  .getDeclaringClass().getName());
        if (definedParameter.name().isEmpty())
          // Fallback not possible. Reflection does not contain parameter name, just returns e.g. arg1
          // Name of parameter required. Name missing at function '%1$s' in class '%2$s'.
          throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_PARAM_NAME_REQUIRED,
              javaAction.getName(), javaAction.getDeclaringClass().getName());
        final JPAParameter parameter = new IntermediateOperationParameter(
            nameBuilder,
            definedParameter,
            nameBuilder.buildPropertyName(definedParameter.name()),
            declaredParameter.getName(),
            types[i],
            getAnnotationInformation());
        parameterList.add(parameter);
      }
    }
    return parameterList;
  }

  @Override
  @CheckForNull
  public JPAParameter getParameter(final Parameter declaredParameter) throws ODataJPAModelException {
    for (final JPAParameter param : getParameter()) {
      if (param.getInternalName().equals(declaredParameter.getName()))
        return param;
    }
    return null;
  }

  @Override
  public JPAOperationResultParameter getResultParameter() {
    return new IntermediateOperationResultParameter(this, jpaAction.returnType(), javaAction.getReturnType(),
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
      parameter.setMapping(new CsdlMapping()
          .setInternalName(getInternalName())
          .setMappedJavaClass(jpaParameter.getType()));
      parameters.add(parameter);
    }
    if (jpaAction.isBound() && bindingPosition.getPosition() != 1)
      // Binding parameter not found within in interface of method %1$s of class %2$s. Binding parameter must be the
      // first parameter.
      throw new ODataJPAModelException(ACTION_PARAM_BINDING_NOT_FOUND,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    return parameters;
  }

  private FullQualifiedName determineParameterType(final BindingPosition bindingPosition, final int i,
      final JPAParameter jpaParameter) throws ODataJPAModelException {

    final EdmPrimitiveTypeKind edmType = JPATypeConverter.convertToEdmSimpleType(jpaParameter.getType());
    if (edmType != null)
      return edmType.getFullQualifiedName();
    final IntermediateEnumerationType enumType = schema.getEnumerationType(jpaParameter.getType());
    if (enumType != null) {
      return enumType.getExternalFQN();
    } else {
      final IntermediateStructuredType<?> structuredType = schema.getEntityType(jpaParameter.getType());
      if (structuredType != null) {
        if (bindingPosition.getPosition() == 0)
          bindingPosition.setPosition(i + 1);
        return structuredType.getExternalFQN();
      } else {
        // The type of %1$s of action of method %2$s of class %1$s could not be converted
        throw new ODataJPAModelException(ACTION_PARAM_ONLY_PRIMITIVE,
            jpaParameter.getInternalName(), javaAction.getName(), javaAction.getDeclaringClass().getName());
      }
    }
  }

  @Override
  protected boolean hasImport() {
    // 13.5 Element edm:ActionImport:
    // The edm:ActionImport element allows exposing an unbound action as a top-level element in an entity container.
    // Action imports are never advertised in the service document.
    return !jpaAction.isBound();
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmAction == null) {
      // TODO handle annotations
      edmAction = new CsdlAction();
      edmAction.setBound(jpaAction.isBound());
      edmAction.setName(getExternalName());
      edmAction.setParameters(returnNullIfEmpty(determineEdmInputParameter()));
      edmAction.setReturnType(determineEdmResultType(jpaAction.returnType(), javaAction));
      edmAction.setEntitySetPath(setEntitySetPath());
      determineUserGroups(this.jpaAction.visibleFor());
    }
  }

  @Override
  CsdlAction getEdmItem() throws ODataJPAModelException {
    if (edmAction == null) {
      lazyBuildEdmItem();
    }
    return edmAction;
  }

  @Override
  boolean isBound() throws ODataJPAModelException {
    return getEdmItem().isBound();
  }

  private CsdlReturnType determineEdmResultType(final ReturnType definedReturnType, final Method javaOperation)
      throws ODataJPAModelException {
    final Class<?> declaredReturnType = javaOperation.getReturnType();
    if (declaredReturnType == void.class)
      return null;

    final CsdlReturnType edmResultType = new CsdlReturnType();
    if (IntermediateOperationHelper.isCollection(declaredReturnType)) {
      if (definedReturnType.type() == Object.class)
        // Type parameter expected for %1$s
        throw new ODataJPAModelException(ACTION_RETURN_TYPE_EXP, javaOperation.getName(), javaOperation
            .getName());
      edmResultType.setCollection(true);
      edmResultType.setType(IntermediateOperationHelper.determineReturnType(definedReturnType, definedReturnType.type(),
          schema, javaOperation.getName()));
    } else {
      edmResultType.setCollection(false);
      edmResultType.setType(IntermediateOperationHelper.determineReturnType(definedReturnType, declaredReturnType,
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

  private String setEntitySetPath() throws ODataJPAModelException {
    if (jpaAction.entitySetPath() == null || jpaAction.entitySetPath().isEmpty())
      return null;
    if (!jpaAction.isBound())
      // Entity Set Path shall only provided for bound actions. Action method %1$s of class %2$s is unbound.
      throw new ODataJPAModelException(ACTION_UNBOUND_ENTITY_SET,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    if (schema.getEntityType(javaAction.getReturnType()) == null)
      throw new ODataJPAModelException(ACTION_UNBOUND_ENTITY_SET,
          javaAction.getName(), javaAction.getDeclaringClass().getName());
    return jpaAction.entitySetPath();
  }

  private static class BindingPosition {
    private Integer position = 0;

    Integer getPosition() {
      return position;
    }

    void setPosition(final Integer position) {
      this.position = position;
    }

  }

  @Override
  public String toString() {
    return "IntermediateJavaAction [jpaAction=" + jpaAction.name() + ", javaAction=" + javaAction
        .getName() + ", javaConstructor=" + javaConstructor.getName() + ", parameterList=" + parameterList + "]";
  }
}
