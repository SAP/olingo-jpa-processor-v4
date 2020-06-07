package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateDataBaseFunction extends IntermediateFunction implements JPADataBaseFunction {
  private final Class<?> jpaDefiningPOJO;

  IntermediateDataBaseFunction(final JPAEdmNameBuilder nameBuilder, final EdmFunction jpaFunction,
      final Class<?> definingPOJO, final IntermediateSchema schema) {

    super(nameBuilder, jpaFunction, schema, IntNameBuilder.buildFunctionName(jpaFunction));
    this.setExternalName(jpaFunction.name());
    this.jpaDefiningPOJO = definingPOJO;
  }

  @Override
  public String getDBName() {
    return jpaFunction.functionName();
  }

  @Override
  public EdmFunctionType getFunctionType() {
    return EdmFunctionType.UserDefinedFunction;
  }

  @Override
  public List<JPAParameter> getParameter() {
    final List<JPAParameter> parameterList = new ArrayList<>();
    for (final EdmParameter jpaParameter : jpaFunction.parameter()) {
      parameterList.add(new IntermediatFunctionParameter(jpaParameter));
    }
    return parameterList;
  }

  @Override
  public JPAParameter getParameter(String internalName) {
    for (JPAParameter parameter : getParameter()) {
      if (parameter.getInternalName().equals(internalName))
        return parameter;
    }
    return null;
  }

  @Override
  public JPAOperationResultParameter getResultParameter() {
    return new IntermediatOperationResultParameter(this, jpaFunction.returnType(),
        jpaFunction.returnType().type().equals(Object.class) ? schema.getEntityType(jpaDefiningPOJO).getTypeClass()
            : jpaFunction.returnType().type());
  }

  @Override
  public CsdlReturnType getReturnType() {
    return edmFunction.getReturnType();
  }

  @Override
  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    int noParameterToSkip = 0;
    final List<CsdlParameter> edmInputParameterList = new ArrayList<>();
    if (jpaFunction.isBound()) {
      noParameterToSkip = ((IntermediateEntityType) schema.getEntityType(this.jpaDefiningPOJO)).getKey().size();
      final CsdlParameter edmInputParameter = new CsdlParameter();
      final IntermediateStructuredType et = schema.getEntityType(jpaDefiningPOJO);
      edmInputParameter.setName("Key");
      edmInputParameter.setType(buildFQN(et.getEdmItem().getName()));
      edmInputParameter.setNullable(false);
      edmInputParameterList.add(edmInputParameter);
    }
    for (int i = noParameterToSkip; i < jpaFunction.parameter().length; i++) {
      final EdmParameter jpaParameter = jpaFunction.parameter()[i];

      final CsdlParameter edmInputParameter = new CsdlParameter();
      edmInputParameter.setName(jpaParameter.name());
      edmInputParameter.setType(determineParameterType(null, jpaParameter));
      edmInputParameter.setNullable(false);
      edmInputParameter.setCollection(jpaParameter.isCollection());
      if (jpaParameter.maxLength() >= 0)
        edmInputParameter.setMaxLength(jpaParameter.maxLength());
      if (jpaParameter.precision() >= 0)
        edmInputParameter.setPrecision(jpaParameter.precision());
      if (jpaParameter.scale() >= 0)
        edmInputParameter.setScale(jpaParameter.scale());
      if (jpaParameter.srid() != null && !jpaParameter.srid().srid().isEmpty()) {
        final SRID srid = SRID.valueOf(jpaParameter.srid().srid());
        srid.setDimension(jpaParameter.srid().dimension());
        edmInputParameter.setSrid(srid);
      }
      edmInputParameterList.add(edmInputParameter);
    }
    return edmInputParameterList;
  }

  // TODO handle multiple schemas
  @Override
  protected CsdlReturnType determineEdmResultType(final ReturnType returnType) throws ODataJPAModelException {

    final CsdlReturnType edmResultType = new CsdlReturnType();
    edmResultType.setType(determineReturnType(returnType));
    edmResultType.setCollection(returnType.isCollection());
    edmResultType.setNullable(returnType.isNullable());
    if (returnType.maxLength() >= 0)
      edmResultType.setMaxLength(returnType.maxLength());
    if (returnType.precision() >= 0)
      edmResultType.setPrecision(returnType.precision());
    if (returnType.scale() >= 0)
      edmResultType.setScale(returnType.scale());
    if (returnType.srid() != null && !returnType.srid().srid().isEmpty()) {
      final SRID srid = SRID.valueOf(returnType.srid().srid());
      srid.setDimension(returnType.srid().dimension());
      edmResultType.setSrid(srid);
    }
    return edmResultType;
  }

  @Override
  protected FullQualifiedName determineParameterType(final Class<?> type, final EdmParameter definedParameter)
      throws ODataJPAModelException {

    final EdmPrimitiveTypeKind edmType = JPATypeConverter.convertToEdmSimpleType(definedParameter.type());
    if (edmType != null)
      return edmType.getFullQualifiedName();
    else {
      final IntermediateEnumerationType enumType = schema.getEnumerationType(definedParameter.type());
      if (enumType != null) {
        return enumType.getExternalFQN();
      } else {
        throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.FUNC_CONV_ERROR);
      }
    }
  }

  private FullQualifiedName determineReturnType(final ReturnType returnType) throws ODataJPAModelException {

    if (returnType.type() == Object.class) {
      final IntermediateStructuredType et = schema.getEntityType(jpaDefiningPOJO);
      this.setIgnore(et.ignore()); // If the result type shall be ignored, ignore also a function that returns it
      return buildFQN(et.getEdmItem().getName());
    } else {
      final IntermediateStructuredType st = schema.getStructuredType(returnType.type());
      if (st != null) {
        this.setIgnore(st.ignore()); // If the result type shall be ignored, ignore also a function that returns it
        return buildFQN(st.getEdmItem().getName());
      } else {
        final IntermediateEnumerationType enumType = schema.getEnumerationType(returnType.type());
        if (enumType != null) {
          return enumType.getExternalFQN();
        } else {
          EdmPrimitiveTypeKind pt = JPATypeConverter.convertToEdmSimpleType(returnType.type());
          if (pt != null)
            return pt.getFullQualifiedName();
          else
            throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_UNKNOWN);
        }
      }
    }
  }
}
