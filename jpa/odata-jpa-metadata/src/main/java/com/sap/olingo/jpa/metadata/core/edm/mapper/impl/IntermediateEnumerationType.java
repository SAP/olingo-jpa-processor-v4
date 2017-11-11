package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumMember;
import org.apache.olingo.commons.api.edm.provider.CsdlEnumType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class IntermediateEnumerationType extends IntermediateModelElement {

  private CsdlEnumType edmEnumType;
  private Class<?> javaEnum;
  private EdmEnumeration annotation;

  IntermediateEnumerationType(JPAEdmNameBuilder nameBuilder, Class<? extends Enum<?>> javaEnum) {
    super(nameBuilder, javaEnum.getSimpleName());
    this.setExternalName(nameBuilder.buildEnumerationTypeName(javaEnum));
    this.javaEnum = javaEnum;
  }

  @Override
  protected void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEnumType == null) {
      annotation = getAnnotation();
      edmEnumType = new CsdlEnumType();
      edmEnumType.setFlags(determineIsFlag());
      edmEnumType.setMembers(buildMembers());
      edmEnumType.setName(getExternalName());
      edmEnumType.setUnderlyingType(determineUnderlyingType());
    }
  }

  @SuppressWarnings("unchecked")
  private <T> FullQualifiedName determineUnderlyingType() throws ODataJPAModelException {
    if (javaEnum.getEnumConstants().length == 0)
      return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();
    try {
      final AttributeConverter<T, ? extends Number> converter = (AttributeConverter<T, ? extends Number>) annotation
          .converter().newInstance();
      final Number value = converter.convertToDatabaseColumn((T) javaEnum.getEnumConstants()[0]);
      return JPATypeConvertor.convertToEdmSimpleType(value.getClass()).getFullQualifiedName();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ODataJPAModelException(e);
    }
  }

  private EdmEnumeration getAnnotation() throws ODataJPAModelException {
    final EdmEnumeration enumAnnotation = javaEnum.getAnnotation(EdmEnumeration.class);
    if (enumAnnotation == null)
      // Annotation EdmEnumeration is missing for Enum %1$s.
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ENUMERATION_ANNOTATION_MISSING, javaEnum
          .getName());
    return enumAnnotation;
  }

  @Override
  CsdlEnumType getEdmItem() throws ODataJPAModelException {
    lazyBuildEdmItem();
    return edmEnumType;
  }

  @SuppressWarnings({ "unchecked" })
  private <T extends Enum<?>> List<CsdlEnumMember> buildMembers() throws ODataJPAModelException {
    final List<CsdlEnumMember> members = new ArrayList<>();
    AttributeConverter<T, ? extends Number> converter = null;

    final List<?> javaMembers = Arrays.asList(javaEnum.getEnumConstants());
    try {
      converter = (AttributeConverter<T, ? extends Number>) annotation.converter().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ODataJPAModelException(e);
    }

    for (final Object constants : javaMembers) {
      if (constants instanceof Enum) {
        final Enum<?> e = (Enum<?>) constants;
        CsdlEnumMember member = new CsdlEnumMember();
        member.setName(e.name());
        member.setValue(String.valueOf(converter.convertToDatabaseColumn((T) e)));
        members.add(member);
      }
    }
    return members;
  }

  private boolean determineIsFlag() {
    return annotation.isFlags();
  }

}
