package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlFunction;
import org.apache.olingo.commons.api.edm.provider.CsdlParameter;
import org.apache.olingo.commons.api.edm.provider.CsdlReturnType;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * Mapper, that is able to convert different metadata resources into a edm function metadata. It is important to know
 * that:
 * <cite>Functions MUST NOT have observable side effects and MUST return a single instance or a collection of instances
 * of any type.</cite>
 * <p>For details about Function metadata see:
 * <a href=
 * "https://docs.oasis-open.org/odata/odata/v4.0/errata02/os/complete/part3-csdl/odata-v4.0-errata02-os-part3-csdl-complete.html#_Toc406398010"
 * >OData Version 4.0 Part 3 - 12.2 Element edm:Function </a>
 * @author Oliver Grande
 *
 */

class IntermediateFunction extends IntermediateModelElement {
  private CsdlFunction edmFunction = null;
  private final EdmFunction jpaStoredProcedure;
  private final IntermediateSchema schema;
  private final Class<?> jpaDefiningPOJO;

  IntermediateFunction(final JPAEdmNameBuilder nameBuilder, final EdmFunction jpaFunction,
      final Class<?> definingPOJO, final IntermediateSchema schema)
          throws ODataJPAModelException {
    super(nameBuilder, intNameBuilder.buildFunctionName(jpaFunction));
    this.setExternalName(jpaFunction.name());
    this.jpaStoredProcedure = jpaFunction;
    this.jpaDefiningPOJO = definingPOJO;
    this.schema = schema;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmFunction == null) {
      edmFunction = new CsdlFunction();
      edmFunction.setName(getExternalName());
      edmFunction.setParameters(returnNullIfEmpty(determineEdmInputParameter()));
      edmFunction.setReturnType(determineEdmResultType(jpaStoredProcedure.returnType()));
      // TODO edmFunction.setBound(isBound)
      // edmFunction.setComposable(isComposable)
    }
  }

  @Override
      CsdlFunction getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmFunction;
  }

  String getStoredProcedure() {
    return jpaStoredProcedure.procedureName();
  }

  private List<CsdlParameter> determineEdmInputParameter() throws ODataJPAModelException {
    List<CsdlParameter> edmInputParameterList = new ArrayList<CsdlParameter>();
    for (EdmFunctionParameter jpaParameter : jpaStoredProcedure.parameter()) {

      CsdlParameter edmInputParameter = new CsdlParameter();
      edmInputParameter.setName(jpaParameter.name());
      edmInputParameter.setType(JPATypeConvertor.convertToEdmSimpleType(jpaParameter.type()).getFullQualifiedName());
      // edmInputParameter.setType(jpaParameter.type());
      edmInputParameter.setNullable(false);
      // TODO edmInputParameter.setCollection(isCollection)
      // TODO edmInputParameter.setSrid(srid)
      if (jpaParameter.maxLength() >= 0)
        edmInputParameter.setMaxLength(jpaParameter.maxLength());
      if (jpaParameter.precision() >= 0)
        edmInputParameter.setPrecision(jpaParameter.precision());
      if (jpaParameter.scale() >= 0)
        edmInputParameter.setScale(jpaParameter.scale());

      edmInputParameterList.add(edmInputParameter);
    }
    return edmInputParameterList;
  }

  // TODO handle multiple schemas
  private CsdlReturnType determineEdmResultType(ReturnType returnType) throws ODataJPAModelException {
    CsdlReturnType edmResultType = new CsdlReturnType();
    FullQualifiedName fqn;
    if (returnType.type() == Object.class)
      fqn = nameBuilder.buildFQN(schema.getEntityType(jpaDefiningPOJO).getEdmItem().getName());
    else {
      IntermediateStructuredType et = schema.getEntityType(returnType.type());
      if (et != null)
        fqn = nameBuilder.buildFQN(et.getEdmItem().getName());
      else
        fqn = JPATypeConvertor.convertToEdmSimpleType(returnType.type()).getFullQualifiedName();
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
    // TODO edmResultType.setSrid(srid);
    return edmResultType;
  }

}
