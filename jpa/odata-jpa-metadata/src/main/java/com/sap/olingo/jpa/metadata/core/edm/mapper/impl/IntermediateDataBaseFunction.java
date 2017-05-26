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
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateDataBaseFunction extends IntermediateFunction {
  private final Class<?> jpaDefiningPOJO;

  IntermediateDataBaseFunction(JPAEdmNameBuilder nameBuilder, EdmFunction jpaFunction, Class<?> definingPOJO,
      IntermediateSchema schema) throws ODataJPAModelException {
    super(nameBuilder, jpaFunction, schema, IntNameBuilder.buildFunctionName(jpaFunction));
    this.setExternalName(jpaFunction.name());
    this.jpaDefiningPOJO = definingPOJO;
  }

  // TODO handle multiple schemas
  @Override
  protected CsdlReturnType determineEdmResultType(final ReturnType returnType) throws ODataJPAModelException {

    final CsdlReturnType edmResultType = new CsdlReturnType();
    FullQualifiedName fqn;
    if (returnType.type() == Object.class) {
      final IntermediateStructuredType et = schema.getEntityType(jpaDefiningPOJO);
      fqn = nameBuilder.buildFQN(et.getEdmItem().getName());
      this.setIgnore(et.ignore()); // If the result type shall be ignored, ignore also a function that returns it
    } else {
      final IntermediateStructuredType st = schema.getStructuredType(returnType.type());
      if (st != null) {
        fqn = nameBuilder.buildFQN(st.getEdmItem().getName());
        this.setIgnore(st.ignore()); // If the result type shall be ignored, ignore also a function that returns it
      } else {
        EdmPrimitiveTypeKind pt = JPATypeConvertor.convertToEdmSimpleType(returnType.type());
        if (pt != null)
          fqn = pt.getFullQualifiedName();
        else
          throw new ODataJPAModelException(MessageKeys.FUNC_RETURN_TYPE_UNKNOWN);
      }
    }
    edmResultType.setType(fqn);
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
  protected List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {

    final List<CsdlParameter> edmInputParameterList = new ArrayList<CsdlParameter>();
    for (final EdmFunctionParameter jpaParameter : jpaUserDefinedFunction.parameter()) {

      final CsdlParameter edmInputParameter = new CsdlParameter();
      edmInputParameter.setName(jpaParameter.name());
      edmInputParameter.setType(JPATypeConvertor.convertToEdmSimpleType(jpaParameter.type()).getFullQualifiedName());

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
}
