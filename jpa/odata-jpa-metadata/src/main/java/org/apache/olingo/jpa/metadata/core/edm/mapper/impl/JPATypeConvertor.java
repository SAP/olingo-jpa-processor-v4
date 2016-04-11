package org.apache.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * This class holds utility methods for Type conversions between JPA and OData Types. First step support V2 Types
 * 
 * 
 */
public final class JPATypeConvertor {

  public static EdmPrimitiveTypeKind convertToEdmSimpleType(final Class<?> type) throws ODataJPAModelException {
    return convertToEdmSimpleType(type, null);
  }

  public static EdmPrimitiveTypeKind convertToEdmSimpleType(final JPAAttribute attribute)
      throws ODataJPAModelException {
    return convertToEdmSimpleType(attribute.getType(), null);
  }

  /**
   * This utility method converts a given jpa Type to equivalent EdmPrimitiveTypeKind for maintaining compatibility
   * between Java and OData Types.
   * 
   * @param jpaType
   * The JPA Type input.
   * @return The corresponding EdmPrimitiveTypeKind.
   * @throws ODataJPAModelException
   * @throws org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException
   * 
   * @see EdmPrimitiveTypeKind
   */

  public static EdmPrimitiveTypeKind convertToEdmSimpleType(final Class<?> jpaType,
      final Attribute<?, ?> currentAttribute) throws ODataJPAModelException {
    if (jpaType.equals(String.class) || jpaType.equals(Character.class) || jpaType.equals(char.class) || jpaType.equals(
        char[].class) || jpaType.equals(Character[].class)) {
      return EdmPrimitiveTypeKind.String;
    } else if (jpaType.equals(Long.class) || jpaType.equals(long.class)) {
      return EdmPrimitiveTypeKind.Int64;
    } else if (jpaType.equals(Short.class) || jpaType.equals(short.class)) {
      return EdmPrimitiveTypeKind.Int16;
    } else if (jpaType.equals(Integer.class) || jpaType.equals(int.class)) {
      return EdmPrimitiveTypeKind.Int32;
    } else if (jpaType.equals(Double.class) || jpaType.equals(double.class)) {
      return EdmPrimitiveTypeKind.Double;
    } else if (jpaType.equals(Float.class) || jpaType.equals(float.class)) {
      return EdmPrimitiveTypeKind.Single;
    } else if (jpaType.equals(BigDecimal.class)) {
      return EdmPrimitiveTypeKind.Decimal;
    } else if (jpaType.equals(byte[].class)) {
      return EdmPrimitiveTypeKind.Binary;
    } else if (jpaType.equals(Byte.class) || jpaType.equals(byte.class)) {
      return EdmPrimitiveTypeKind.Byte;
    } else if (jpaType.equals(Boolean.class) || jpaType.equals(boolean.class)) {
      return EdmPrimitiveTypeKind.Boolean;
    } else if (jpaType.equals(java.sql.Time.class)) {
      // TODO Check mapping change
      return EdmPrimitiveTypeKind.TimeOfDay;
    } else if (jpaType.equals(java.time.LocalDate.class)) {
      // TODO Check mapping enhancement
      return EdmPrimitiveTypeKind.Date;
    } else if (jpaType.equals(Date.class) || jpaType.equals(
        Calendar.class) || jpaType.equals(Timestamp.class) || jpaType.equals(java.util.Date.class)) {
      if ((currentAttribute != null) && (determineTemporalType(currentAttribute) == TemporalType.TIME)) {
        // TODO Check mapping change from Time
        return EdmPrimitiveTypeKind.TimeOfDay;
      } else {
        // TODO Check mapping change from DateTime
        return EdmPrimitiveTypeKind.DateTimeOffset;
      }
    } else if (jpaType.equals(UUID.class)) {
      return EdmPrimitiveTypeKind.Guid;
    } else if (jpaType.equals(Byte[].class)) {
      return EdmPrimitiveTypeKind.Binary;
    } else if (jpaType.equals(Blob.class) && isBlob(currentAttribute)) {
      return EdmPrimitiveTypeKind.Binary;
    } else if (jpaType.equals(Clob.class) && isBlob(currentAttribute)) {
      return EdmPrimitiveTypeKind.String;
    } else if (jpaType.isEnum()) {
      return EdmPrimitiveTypeKind.String;
    }

    throw ODataJPAModelException.throwException(ODataJPAModelException.TYPE_NOT_SUPPORTED,
        "Type of attribute is not supporte. Mapping not possible");
  }

  private static TemporalType determineTemporalType(final Attribute<?, ?> currentAttribute) {
    if (currentAttribute != null) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Temporal.class) != null) {
        return annotatedElement.getAnnotation(Temporal.class).value();
      }
    }
    return null;

  }

  private static boolean isBlob(final Attribute<?, ?> currentAttribute) {
    if (currentAttribute != null) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Lob.class) != null) {
        return true;
      }
    }
    return false;
  }
}
