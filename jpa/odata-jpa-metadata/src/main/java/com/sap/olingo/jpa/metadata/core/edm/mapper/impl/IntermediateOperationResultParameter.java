package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import javax.annotation.CheckForNull;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.geo.SRID;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction.ReturnType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperation;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;

class IntermediateOperationResultParameter implements JPAOperationResultParameter {
  /**
   *
   */
  private final JPAOperation jpaOperation;
  private final ReturnType jpaReturnType;
  private final Class<?> type;
  private final boolean isCollection;

  public IntermediateOperationResultParameter(final JPAOperation jpaOperation, final ReturnType jpaReturnType,
      final Class<?> returnType, final boolean isCollection) {
    this.jpaOperation = jpaOperation;
    this.jpaReturnType = jpaReturnType;
    this.isCollection = isCollection;
    if (isCollection)
      this.type = jpaReturnType.type();
    else
      this.type = returnType;
  }

  public IntermediateOperationResultParameter(final JPAOperation jpaOperation, final ReturnType jpaReturnType,
      final Class<?> returnType) {
    this.jpaOperation = jpaOperation;
    this.jpaReturnType = jpaReturnType;
    this.isCollection = jpaReturnType.isCollection();
    this.type = returnType;
  }

  @Override
  public Class<?> getType() {
    return type;
  }

  @Override
  public Integer getMaxLength() {
    return jpaReturnType.maxLength();
  }

  @Override
  public Integer getPrecision() {
    return jpaReturnType.precision();
  }

  @Override
  public Integer getScale() {
    return jpaReturnType.scale();
  }

  @Override
  public FullQualifiedName getTypeFQN() {
    return jpaOperation.getReturnType().getTypeFQN();
  }

  @Override
  public boolean isCollection() {
    return isCollection;
  }

  @Override
  @CheckForNull
  public SRID getSrid() {
    if (jpaReturnType.srid().srid().isEmpty())
      return null;
    final SRID srid = SRID.valueOf(jpaReturnType.srid().srid());
    srid.setDimension(jpaReturnType.srid().dimension());
    return srid;
  }

}