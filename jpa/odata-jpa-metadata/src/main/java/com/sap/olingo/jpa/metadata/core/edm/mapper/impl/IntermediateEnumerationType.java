package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEdmNameBuilder;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class IntermediateEnumerationType extends IntermediateModelElement implements JPAEnumerationAttribute {

  private CsdlEnumType edmEnumType;
  private final Class<?> javaEnum;
  private EdmEnumeration annotation;
  private List<?> javaMembers;

  IntermediateEnumerationType(final JPAEdmNameBuilder nameBuilder, final Class<? extends Enum<?>> javaEnum,
      final IntermediateAnnotationInformation annotationInfo) {
    super(nameBuilder, javaEnum.getSimpleName(), annotationInfo);
    this.setExternalName(nameBuilder.buildEnumerationTypeName(javaEnum));
    this.javaEnum = javaEnum;
  }

  @Override
  public Object convert(final List<String> values) throws ODataJPAModelException {
    if (values == null || values.isEmpty())
      return null;
    if (annotation.converter() == EdmEnumeration.DummyConverter.class)
      return enumOf(values.get(0));
    else {
      final Enum<?>[] array = getArray(javaEnum, values.size(), null);
      for (int i = 0; i < values.size(); i++) {
        array[i] = enumOf(values.get(i));
      }
      return array;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Enum<?>> T enumOf(final String value) throws ODataJPAModelException {
    if (edmEnumType == null) {
      lazyBuildEdmItem();
    }
    for (final Object member : javaMembers)
      if (((T) member).name().equals(value))
        return (T) member;
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T extends Number, E extends Enum<E>> E enumOf(final T value) throws ODataJPAModelException {
    if (edmEnumType == null) {
      lazyBuildEdmItem();
    }
    if (annotation.converter() != DummyConverter.class) {
      return (E) (createConverter().convertToEntityAttribute(value)[0]);
    } else {
      for (final Object member : javaMembers)
        if (((Enum<?>) member).ordinal() == (Integer) value)
          return (E) member;
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private <T extends Number> AttributeConverter<Enum<?>[], T> createConverter() throws ODataJPAModelException {
    try {
      return (AttributeConverter<Enum<?>[], T>) annotation.converter().getDeclaredConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      throw new ODataJPAModelException(e);
    }
  }

  @Override
  public boolean isFlags() throws ODataJPAModelException {
    if (edmEnumType == null) {
      lazyBuildEdmItem();
    }
    return edmEnumType.isFlags();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Number> T valueOf(final String value) throws ODataJPAModelException {

    final Enum<?>[] array = getArray(javaEnum, 1, enumOf(value));
    return (T) createConverter().convertToDatabaseColumn(array);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends Number> T valueOf(final List<String> values) throws ODataJPAModelException {
    if (values == null || values.isEmpty())
      return null;
    if (annotation.converter() == EdmEnumeration.DummyConverter.class)
      return valueOf(values.get(0));
    else {
      return (T) createConverter().convertToDatabaseColumn((Enum<?>[]) convert(values));
    }
  }

  @Override
  protected synchronized void lazyBuildEdmItem() throws ODataJPAModelException {
    if (edmEnumType == null) {
      annotation = getAnnotation();
      edmEnumType = new CsdlEnumType();
      edmEnumType.setFlags(determineIsFlag());
      edmEnumType.setMembers(buildMembers());
      edmEnumType.setName(getExternalName());
      edmEnumType.setUnderlyingType(determineUnderlyingType());
    }
  }

  @Override
  CsdlEnumType getEdmItem() throws ODataJPAModelException {
    if (edmEnumType == null) {
      lazyBuildEdmItem();
    }
    return edmEnumType;
  }

  private List<CsdlEnumMember> buildMembers() throws ODataJPAModelException {
    final List<CsdlEnumMember> members = new ArrayList<>();

    javaMembers = Arrays.asList(javaEnum.getEnumConstants());

    for (final Object constants : javaMembers) {
      if (constants instanceof Enum) {
        final Enum<?> e = (Enum<?>) constants;
        final CsdlEnumMember member = new CsdlEnumMember();
        member.setName(e.name());
        final Number value = valueOf(e.toString());
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

  private <T extends Number> FullQualifiedName determineUnderlyingType() throws ODataJPAModelException {
    if (javaEnum.getEnumConstants().length == 0)
      return EdmPrimitiveTypeKind.Int32.getFullQualifiedName();

    final T value = valueOf(javaEnum.getEnumConstants()[0].toString());
    final EdmPrimitiveTypeKind type = JPATypeConverter.convertToEdmSimpleType(value.getClass());
    if (isValidType(type))
      return type.getFullQualifiedName();
    // Enumeration '%1$s' has the unsupported type '%2$s'.
    throw new ODataJPAModelException(MessageKeys.ENUMERATION_UNSUPPORTED_TYPE, javaEnum.getName(), type
        .getFullQualifiedName().getFullQualifiedNameAsString());

  }

  private EdmEnumeration getAnnotation() throws ODataJPAModelException {
    final EdmEnumeration enumAnnotation = javaEnum.getAnnotation(EdmEnumeration.class);
    if (enumAnnotation == null)
      // Annotation EdmEnumeration is missing for Enum %1$s.
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.ENUMERATION_ANNOTATION_MISSING, javaEnum
          .getName());
    return enumAnnotation;
  }

  @SuppressWarnings("unchecked")
  private <E extends Enum<?>> E[] getArray(final Class<?> javaEnum, final int size, final Enum<?> e) {
    final E[] arr = (E[]) Array.newInstance(javaEnum, size);
    arr[0] = (E) e;
    return arr;
  }

  private boolean isValidType(final EdmPrimitiveTypeKind type) {
    // "Edm.Byte, Edm.SByte, Edm.Int16, Edm.Int32, or Edm.Int64."
    return type == EdmPrimitiveTypeKind.Byte
        || type == EdmPrimitiveTypeKind.Int16
        || type == EdmPrimitiveTypeKind.Int32
        || type == EdmPrimitiveTypeKind.Int64
        || type == EdmPrimitiveTypeKind.SByte;
  }

}
