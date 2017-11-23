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
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration.DummyConverter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateEnumerationType extends IntermediateModelElement implements JPAEnumerationAttribute {

  private CsdlEnumType edmEnumType;
  private Class<?> javaEnum;
  private EdmEnumeration annotation;
  private List<?> javaMembers;

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
      final EdmPrimitiveTypeKind type = JPATypeConvertor.convertToEdmSimpleType(value.getClass());
      if (isValidType(type))
        return type.getFullQualifiedName();
      // Enumeration '%1$s' has the unsupported type '%2$s'.
      throw new ODataJPAModelException(MessageKeys.ENUMERATION_UNSUPPORTED_TYPE, javaEnum.getName(), type
          .getFullQualifiedName().getFullQualifiedNameAsString());
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ODataJPAModelException(e);
    }
  }

  private boolean isValidType(EdmPrimitiveTypeKind type) {
    // "Edm.Byte, Edm.SByte, Edm.Int16, Edm.Int32, or Edm.Int64."
    return type == EdmPrimitiveTypeKind.Byte
        || type == EdmPrimitiveTypeKind.Int16
        || type == EdmPrimitiveTypeKind.Int32
        || type == EdmPrimitiveTypeKind.Int64
        || type == EdmPrimitiveTypeKind.SByte;
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

    javaMembers = Arrays.asList(javaEnum.getEnumConstants());
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
        Number value = converter.convertToDatabaseColumn((T) e);
        if (determineIsFlag() && value.longValue() < 0L)
          // An Enumeration that is marked as Flag must not have a negative value: '%1$s - %2$s'.
          throw new ODataJPAModelException(MessageKeys.ENUMERATION_NO_NEGATIVE_VALUE, e.name(), javaEnum
              .getName());
        member.setValue(String.valueOf(value));
        members.add(member);
      }
    }
    return members;
  }

  private boolean determineIsFlag() {
    return annotation.isFlags();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Enum<?>> T enumOf(final String value) throws ODataJPAModelException {
    lazyBuildEdmItem();
    for (Object member : javaMembers)
      if (((T) member).name().equals(value))
        return (T) member;
    return null;
  }

  @Override
  public <T extends Number> Enum<?> enumOf(final T value) throws ODataJPAModelException {
    lazyBuildEdmItem();

    if (annotation.converter() != DummyConverter.class) {
      try {
        @SuppressWarnings("unchecked")
        final AttributeConverter<?, T> converter = (AttributeConverter<?, T>) annotation.converter().newInstance();
        return (Enum<?>) converter.convertToEntityAttribute(value);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new ODataJPAModelException(e);
      }
    } else {
      for (Object member : javaMembers)
        if (((Enum<?>) member).ordinal() == (Integer) value)
          return (Enum<?>) member;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <E extends Enum<?>, T extends Number> T valueOf(final String value) throws ODataJPAModelException {
    try {
      AttributeConverter<E, T> converter = (AttributeConverter<E, T>) annotation.converter().newInstance();
      return converter.convertToDatabaseColumn(enumOf(value));
    } catch (InstantiationException | IllegalAccessException e) {
      throw new ODataJPAModelException(e);
    }
  }

}
