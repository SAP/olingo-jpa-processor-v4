package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateOperationParameter extends IntermediateModelElement implements JPAParameter {
  private final EdmParameter jpaParameter;
  private final String externalName;
  private final Class<?> type;

  IntermediateOperationParameter(final JPAEdmNameBuilder nameBuilder, final EdmParameter jpaParameter,
      final String externalName, final String internalName, final Class<?> type,
      final IntermediateAnnotationInformation annotationInfo) {
    super(nameBuilder, internalName, annotationInfo);
    this.jpaParameter = jpaParameter;
    this.externalName = externalName;
    this.type = type;
  }

  @Override
  public String getInternalName() {
    return internalName;
  }

  @Override
  public String getName() {
    return externalName;
  }

  @Override
  public Class<?> getType() {
    return type.isPrimitive() ? boxPrimitive(type) : type;
  }

  @Override
  public Integer getMaxLength() {
    return jpaParameter.maxLength();
  }

  @Override
  public Integer getPrecision() {
    return jpaParameter.precision();
  }

  @Override
  public Integer getScale() {
    return jpaParameter.scale();
  }

  @Override
  public FullQualifiedName getTypeFQN() throws ODataJPAModelException {
    return JPATypeConverter.convertToEdmSimpleType(jpaParameter.type()).getFullQualifiedName();
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    // No build needed, as IntermediateOperationParameter is a facade for EdmParameter annotation
  }

  @Override
  CsdlAbstractEdmItem getEdmItem() throws ODataJPAModelException {
    return null;
  }

  @Override
  public SRID getSrid() {
    if (jpaParameter.srid() != null && !jpaParameter.srid().srid().isEmpty()) {
      final SRID srid = SRID.valueOf(jpaParameter.srid().srid());
      srid.setDimension(jpaParameter.srid().dimension());
      return srid;
    } else {
      return null;
    }
  }
}
