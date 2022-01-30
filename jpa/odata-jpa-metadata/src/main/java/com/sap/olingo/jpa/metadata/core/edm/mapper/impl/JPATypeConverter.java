package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

/**
 * This class holds utility methods for type conversions between JPA Java types and OData Types.
 *
 */
public final class JPATypeConverter {
  private static Set<Class<?>> olingoSupportedTypes = typesSupportedByOlingo();
  private static Set<Class<?>> scalarTypes = typesScalar();

  private JPATypeConverter() {}

  private static Set<Class<?>> typesSupportedByOlingo() {
    final Set<Class<?>> types = new HashSet<>(32);
    types.add(Boolean.class);
    types.add(Byte.class);
    types.add(Byte[].class);
    types.add(byte[].class);
    types.add(Double.class);
    types.add(Float.class);
    types.add(Integer.class);
    types.add(java.math.BigDecimal.class);
    types.add(java.math.BigInteger.class);
    types.add(java.sql.Time.class);
    types.add(java.sql.Timestamp.class);
    types.add(java.util.Calendar.class);
    types.add(java.time.LocalTime.class);
    types.add(java.time.LocalDate.class);
    types.add(java.time.ZonedDateTime.class);
    types.add(java.time.Instant.class);
    types.add(java.util.Date.class);
    types.add(java.util.UUID.class);
    types.add(Long.class);
    types.add(Short.class);
    types.add(String.class);
    return types;
  }

  private static Set<Class<?>> typesScalar() {
    final Set<Class<?>> types = new HashSet<>(32);
    types.add(String.class);
    types.add(Character.class);
    types.add(Long.class);
    types.add(Short.class);
    types.add(Integer.class);
    types.add(Double.class);
    types.add(Float.class);
    types.add(BigDecimal.class);
    types.add(BigInteger.class);
    types.add(Byte.class);
    types.add(Boolean.class);
    types.add(java.sql.Time.class);
    types.add(java.time.LocalTime.class);
    types.add(java.time.Duration.class);
    types.add(java.time.LocalDate.class);
    types.add(java.time.OffsetDateTime.class);
    types.add(java.time.ZonedDateTime.class);
    types.add(java.time.Instant.class);
    types.add(java.sql.Date.class);
    types.add(Calendar.class);
    types.add(Timestamp.class);
    types.add(java.util.Date.class);
    types.add(UUID.class);
    return types;
  }
  
  public static EdmPrimitiveTypeKind convertToEdmSimpleType(final Class<?> type) throws ODataJPAModelException {
    return convertToEdmSimpleType(type, null);
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

  public static <T> EdmPrimitiveTypeKind convertToEdmSimpleType(final Class<T> jpaType, // NOSONAR
      final Attribute<?, ?> currentAttribute) throws ODataJPAModelException {

    if (jpaType.equals(String.class) || jpaType.equals(Character.class) || jpaType.equals(char.class) || jpaType.equals(
        char[].class) || jpaType.equals(Character[].class))
      return EdmPrimitiveTypeKind.String;
    else if (jpaType.equals(Long.class) || jpaType.equals(long.class))
      return EdmPrimitiveTypeKind.Int64;
    else if (jpaType.equals(Short.class) || jpaType.equals(short.class))
      return EdmPrimitiveTypeKind.Int16;
    else if (jpaType.equals(Integer.class) || jpaType.equals(int.class))
      return EdmPrimitiveTypeKind.Int32;
    else if (jpaType.equals(Double.class) || jpaType.equals(double.class))
      return EdmPrimitiveTypeKind.Double;
    else if (jpaType.equals(Float.class) || jpaType.equals(float.class))
      return EdmPrimitiveTypeKind.Single;
    else if (jpaType.equals(BigDecimal.class) || jpaType.equals(BigInteger.class))
      return EdmPrimitiveTypeKind.Decimal;
    else if (jpaType.equals(byte[].class))
      return EdmPrimitiveTypeKind.Binary;
    else if (jpaType.equals(Byte.class) || jpaType.equals(byte.class))
      return EdmPrimitiveTypeKind.SByte;
    else if (jpaType.equals(Boolean.class) || jpaType.equals(boolean.class))
      return EdmPrimitiveTypeKind.Boolean;
    else if (jpaType.equals(java.time.LocalTime.class) || jpaType.equals(java.sql.Time.class))
      return EdmPrimitiveTypeKind.TimeOfDay;
    else if (jpaType.equals(java.time.Duration.class))
      return EdmPrimitiveTypeKind.Duration;
    else if (jpaType.equals(java.time.LocalDate.class) || jpaType.equals(java.sql.Date.class))
      return EdmPrimitiveTypeKind.Date;
    else if (jpaType.equals(Calendar.class) || jpaType.equals(Timestamp.class) || jpaType.equals(
        java.util.Date.class)) {
      if ((currentAttribute != null) && (determineTemporalType(currentAttribute) == TemporalType.TIME))
        return EdmPrimitiveTypeKind.TimeOfDay;
      else if ((currentAttribute != null) && (determineTemporalType(currentAttribute) == TemporalType.DATE))
        return EdmPrimitiveTypeKind.Date;
      else
        return EdmPrimitiveTypeKind.DateTimeOffset;
    } else if (jpaType.equals(ZonedDateTime.class) || jpaType.equals(LocalDateTime.class) // NOSONAR
        || jpaType.equals(OffsetDateTime.class) || jpaType.equals(Instant.class))
      // Looks like Olingo does not support LocalDateTime or OffsetDateTime, which are supported by JPA 2.2. Olingo only
      // takes ZonedDateTime.
      return EdmPrimitiveTypeKind.DateTimeOffset;
    else if (jpaType.equals(UUID.class))
      return EdmPrimitiveTypeKind.Guid;
    else if (jpaType.equals(Blob.class) && isBlob(currentAttribute))
      return EdmPrimitiveTypeKind.Binary;
    else if (jpaType.equals(Clob.class) && isBlob(currentAttribute))
      return EdmPrimitiveTypeKind.String;
    else if (isGeography(currentAttribute))
      return convertGeography(jpaType, currentAttribute);
    else if (isGeometry(currentAttribute)) return convertGeometry(jpaType, currentAttribute);
    if (currentAttribute != null)
      // Type (%1$s) of attribute (%2$s) is not supported. Mapping not possible
      throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TYPE_NOT_SUPPORTED,
          jpaType.getName(), currentAttribute.getName());
    else
      return null;
  }

  public static EdmPrimitiveTypeKind convertToEdmSimpleType(final JPAAttribute attribute)
      throws ODataJPAModelException {
    return convertToEdmSimpleType(attribute.getType(), null);
  }

  public static boolean isSimpleType(final Class<?> type, final Attribute<?, ?> currentAttribute) {
    return type != null
        && (isScalarType(type)
            || type.equals(Byte[].class)
            || type.equals(Blob.class)
            || type.equals(Clob.class)
            || isGeography(currentAttribute)
            || isGeometry(currentAttribute));
  }

  public static boolean isScalarType(final Class<?> type) {
    return scalarTypes.contains(type);
  }

  /**
   * For supported java types see {@link org.apache.olingo.commons.api.edm.EdmPrimitiveType}. In addition, since 4.7.1,
   * also some types from the java.time package are supported, see:
   * <ul>
   * <li>For EdmDate: LocalDate, see
   * {@link org.apache.olingo.commons.core.edm.primitivetype.EdmDate#internalValueToString
   * EdmDate.internalValueToString}</li>
   * <li>For EdmTimeOfDay: LocalTime, see
   * {@link org.apache.olingo.commons.core.edm.primitivetype.EdmTimeOfDay#internalValueToString
   * EdmTimeOfDay.internalValueToString}</li>
   * <li>For EdmDateTimeOffset: ZonedDateTime, see
   * {@link org.apache.olingo.commons.core.edm.primitivetype.EdmDateTimeOffset#internalValueToString
   * EdmDateTimeOffset.internalValueToString}</li>
   * </ul>
   * @param type
   * @return
   */
  public static boolean isSupportedByOlingo(final Class<?> type) {

    return olingoSupportedTypes.contains(type);
  }

  private static EdmPrimitiveTypeKind convertGeography(final Class<?> jpaType, final Attribute<?, ?> currentAttribute)
      throws ODataJPAModelException {
    
    
    if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.Point.class))
      return EdmPrimitiveTypeKind.GeographyPoint;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.MultiPoint.class))
      return EdmPrimitiveTypeKind.GeographyMultiPoint;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.LineString.class))
      return EdmPrimitiveTypeKind.GeographyLineString;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.MultiLineString.class))
      return EdmPrimitiveTypeKind.GeographyMultiLineString;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.Polygon.class))
      return EdmPrimitiveTypeKind.GeographyPolygon;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.MultiPolygon.class))
      return EdmPrimitiveTypeKind.GeographyMultiPolygon;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.GeospatialCollection.class))
      return EdmPrimitiveTypeKind.GeographyCollection;
    // Type (%1$s) of attribute (%2$s) is not supported. Mapping not possible
    throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TYPE_NOT_SUPPORTED,
        jpaType.getName(), currentAttribute.getName());
  }

  private static EdmPrimitiveTypeKind convertGeometry(final Class<?> jpaType, final Attribute<?, ?> currentAttribute)
      throws ODataJPAModelException {
    if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.Point.class))
      return EdmPrimitiveTypeKind.GeometryPoint;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.MultiPoint.class))
      return EdmPrimitiveTypeKind.GeometryMultiPoint;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.LineString.class))
      return EdmPrimitiveTypeKind.GeometryLineString;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.MultiLineString.class))
      return EdmPrimitiveTypeKind.GeometryMultiLineString;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.Polygon.class))
      return EdmPrimitiveTypeKind.GeometryPolygon;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.MultiPolygon.class))
      return EdmPrimitiveTypeKind.GeometryMultiPolygon;
    else if (jpaType.equals(org.apache.olingo.commons.api.edm.geo.GeospatialCollection.class))
      return EdmPrimitiveTypeKind.GeometryCollection;
    // Type (%1$s) of attribute (%2$s) is not supported. Mapping not possible
    throw new ODataJPAModelException(ODataJPAModelException.MessageKeys.TYPE_NOT_SUPPORTED,
        jpaType.getName(), currentAttribute.getName());
  }

  private static TemporalType determineTemporalType(final Attribute<?, ?> currentAttribute) {
    if (currentAttribute != null) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Temporal.class) != null) return annotatedElement
          .getAnnotation(Temporal.class).value();
    }
    return null;

  }

  private static Dimension getDimension(final Attribute<?, ?> currentAttribute) {
    if (currentAttribute.getJavaMember() instanceof AnnotatedElement) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      final EdmGeospatial spatialDetails = annotatedElement.getAnnotation(EdmGeospatial.class);
      if (spatialDetails != null)
        return spatialDetails.dimension();
    }
    return null;
  }

  private static boolean isBlob(final Attribute<?, ?> currentAttribute) {
    if (currentAttribute != null) {
      final AnnotatedElement annotatedElement = (AnnotatedElement) currentAttribute.getJavaMember();
      if (annotatedElement != null && annotatedElement.getAnnotation(Lob.class) != null) return true;
    }
    return false;
  }

  private static boolean isGeography(final Attribute<?, ?> currentAttribute) {
    return currentAttribute != null && getDimension(currentAttribute) == Dimension.GEOGRAPHY;
  }

  private static boolean isGeometry(final Attribute<?, ?> currentAttribute) {
    return currentAttribute != null && getDimension(currentAttribute) == Dimension.GEOMETRY;
  }
}
