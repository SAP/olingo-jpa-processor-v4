package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;
import java.util.stream.Stream;

import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.metamodel.Attribute;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.geo.Geospatial.Dimension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmGeospatial;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TestJPATypeConvertor {

  static Stream<Arguments> mappingJavaLobToOData() {

    return Stream.of(
        arguments(Blob.class, buildLobAttribute(), EdmPrimitiveTypeKind.Binary),
        arguments(Clob.class, buildLobAttribute(), EdmPrimitiveTypeKind.String));
  }

  static Stream<Arguments> mappingJavaTimeToOData() {
    return Stream.of(
        arguments(java.time.LocalTime.class, null, EdmPrimitiveTypeKind.TimeOfDay),
        arguments(java.sql.Time.class, null, EdmPrimitiveTypeKind.TimeOfDay),
        arguments(java.time.Duration.class, null, EdmPrimitiveTypeKind.Duration),
        arguments(java.time.LocalDate.class, null, EdmPrimitiveTypeKind.Date),
        arguments(java.sql.Date.class, null, EdmPrimitiveTypeKind.Date),
        arguments(Calendar.class, null, EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(Calendar.class, buildTimeAttribute(TemporalType.TIME), EdmPrimitiveTypeKind.TimeOfDay),
        arguments(Calendar.class, buildTimeAttribute(TemporalType.DATE), EdmPrimitiveTypeKind.Date),
        arguments(Calendar.class, buildTimeAttribute(TemporalType.TIMESTAMP), EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(Calendar.class, buildTimeAttribute(null), EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(Timestamp.class, null, EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(Timestamp.class, buildTimeAttribute(TemporalType.TIME), EdmPrimitiveTypeKind.TimeOfDay),
        arguments(Timestamp.class, buildTimeAttribute(TemporalType.DATE), EdmPrimitiveTypeKind.Date),
        arguments(Timestamp.class, buildTimeAttribute(TemporalType.TIMESTAMP), EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(Timestamp.class, buildTimeAttribute(null), EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(java.util.Date.class, null, EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(java.util.Date.class, buildTimeAttribute(TemporalType.TIME), EdmPrimitiveTypeKind.TimeOfDay),
        arguments(java.util.Date.class, buildTimeAttribute(TemporalType.DATE), EdmPrimitiveTypeKind.Date),
        arguments(java.util.Date.class, buildTimeAttribute(TemporalType.TIMESTAMP),
            EdmPrimitiveTypeKind.DateTimeOffset),
        arguments(java.util.Date.class, buildTimeAttribute(null), EdmPrimitiveTypeKind.DateTimeOffset));
  }

  static Stream<Arguments> mappingSimpleJavaToOData() {
    return Stream.of(
        arguments(Long.class, EdmPrimitiveTypeKind.Int64),
        arguments(long.class, EdmPrimitiveTypeKind.Int64),
        arguments(Integer.class, EdmPrimitiveTypeKind.Int32),
        arguments(int.class, EdmPrimitiveTypeKind.Int32),
        arguments(Short.class, EdmPrimitiveTypeKind.Int16),
        arguments(short.class, EdmPrimitiveTypeKind.Int16),
        arguments(String.class, EdmPrimitiveTypeKind.String),
        arguments(Character.class, EdmPrimitiveTypeKind.String),
        arguments(char.class, EdmPrimitiveTypeKind.String),
        arguments(char[].class, EdmPrimitiveTypeKind.String),
        arguments(Character[].class, EdmPrimitiveTypeKind.String),
        arguments(Double.class, EdmPrimitiveTypeKind.Double),
        arguments(double.class, EdmPrimitiveTypeKind.Double),
        arguments(Float.class, EdmPrimitiveTypeKind.Single),
        arguments(float.class, EdmPrimitiveTypeKind.Single),
        arguments(BigDecimal.class, EdmPrimitiveTypeKind.Decimal),
        arguments(Byte.class, EdmPrimitiveTypeKind.SByte),
        arguments(byte.class, EdmPrimitiveTypeKind.SByte),
        arguments(byte[].class, EdmPrimitiveTypeKind.Binary),
        arguments(Boolean.class, EdmPrimitiveTypeKind.Boolean),
        arguments(boolean.class, EdmPrimitiveTypeKind.Boolean),
        arguments(UUID.class, EdmPrimitiveTypeKind.Guid));
  }

  static Stream<Arguments> mappingJavaGeographyToOData() {
    return Stream.of(
        arguments(org.apache.olingo.commons.api.edm.geo.Point.class, buildDimensionAttribute(Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyPoint),
        arguments(org.apache.olingo.commons.api.edm.geo.MultiPoint.class, buildDimensionAttribute(Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyMultiPoint),
        arguments(org.apache.olingo.commons.api.edm.geo.LineString.class, buildDimensionAttribute(Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyLineString),
        arguments(org.apache.olingo.commons.api.edm.geo.MultiLineString.class, buildDimensionAttribute(
            Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyMultiLineString),
        arguments(org.apache.olingo.commons.api.edm.geo.Polygon.class, buildDimensionAttribute(Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyPolygon),
        arguments(org.apache.olingo.commons.api.edm.geo.MultiPolygon.class, buildDimensionAttribute(
            Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyMultiPolygon),
        arguments(org.apache.olingo.commons.api.edm.geo.GeospatialCollection.class, buildDimensionAttribute(
            Dimension.GEOGRAPHY),
            EdmPrimitiveTypeKind.GeographyCollection)

    );
  }

  static Stream<Arguments> mappingJavaGeometryToOData() {
    return Stream.of(
        arguments(org.apache.olingo.commons.api.edm.geo.Point.class, buildDimensionAttribute(Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryPoint),
        arguments(org.apache.olingo.commons.api.edm.geo.MultiPoint.class, buildDimensionAttribute(Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryMultiPoint),
        arguments(org.apache.olingo.commons.api.edm.geo.LineString.class, buildDimensionAttribute(Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryLineString),
        arguments(org.apache.olingo.commons.api.edm.geo.MultiLineString.class, buildDimensionAttribute(
            Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryMultiLineString),
        arguments(org.apache.olingo.commons.api.edm.geo.Polygon.class, buildDimensionAttribute(Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryPolygon),
        arguments(org.apache.olingo.commons.api.edm.geo.MultiPolygon.class, buildDimensionAttribute(
            Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryMultiPolygon),
        arguments(org.apache.olingo.commons.api.edm.geo.GeospatialCollection.class, buildDimensionAttribute(
            Dimension.GEOMETRY),
            EdmPrimitiveTypeKind.GeometryCollection)

    );
  }

  static Stream<Arguments> scalarJavaTypes() {
    return Stream.of(
        arguments(Integer.class, null, true),
        arguments(String.class, null, true),
        arguments(Character.class, null, true),
        arguments(Long.class, null, true),
        arguments(Short.class, null, true),
        arguments(Integer.class, null, true),
        arguments(Double.class, null, true),
        arguments(Float.class, null, true),
        arguments(BigDecimal.class, null, true),
        arguments(Byte.class, null, true),
        arguments(Boolean.class, null, true),
        arguments(java.time.LocalTime.class, null, true),
        arguments(java.sql.Time.class, null, true),
        arguments(java.time.Duration.class, null, true),
        arguments(java.time.LocalDate.class, null, true),
        arguments(java.sql.Date.class, null, true),
        arguments(Calendar.class, null, true),
        arguments(Timestamp.class, null, true),
        arguments(java.util.Date.class, null, true),
        arguments(UUID.class, null, true),
        arguments(Attribute.class, null, false));
  }

  static Stream<Arguments> supportedByOlingo() {

    return Stream.of(
        arguments(Boolean.class, true),
        arguments(Byte.class, true),
        arguments(Byte[].class, true),
        arguments(byte[].class, true),
        arguments(Double.class, true),
        arguments(Float.class, true),
        arguments(Integer.class, true),
        arguments(java.math.BigDecimal.class, true),
        arguments(java.math.BigInteger.class, true),
        arguments(java.sql.Time.class, true),
        arguments(java.sql.Timestamp.class, true),
        arguments(java.util.Calendar.class, true),
        arguments(java.util.Date.class, true),
        arguments(java.util.UUID.class, true),
        arguments(Long.class, true),
        arguments(Short.class, true),
        arguments(String.class, true),
        arguments(LocalDateTime.class, false));
  }

  static Stream<Arguments> simpleJavaTypes() {
    return Stream.concat(scalarJavaTypes(), Stream.of(
        arguments(Integer.class, null, true)));
  }

  private static Attribute<?, ?> buildLobAttribute() {
    final Attribute<?, ?> a = mock(Attribute.class);
    final AnnotatedElement e = mock(AnnotatedElement.class, Mockito.withSettings().extraInterfaces(Member.class));
    final Lob l = mock(Lob.class);

    when(a.getJavaMember()).thenReturn((Member) e);
    when(e.getAnnotation(Lob.class)).thenReturn(l);
    when(a.getName()).thenReturn("Lob");
    return a;
  }

  private static Attribute<?, ?> buildTimeAttribute(final TemporalType time) {
    final Attribute<?, ?> a = mock(Attribute.class);
    final AnnotatedElement e = mock(AnnotatedElement.class, Mockito.withSettings().extraInterfaces(Member.class));
    final Temporal t = mock(Temporal.class);

    when(a.getJavaMember()).thenReturn((Member) e);
    when(e.getAnnotation(Temporal.class)).thenReturn(t);
    when(t.value()).thenReturn(time);

    return a;
  }

  private static Attribute<?, ?> buildDimensionAttribute(final Dimension dimension) {
    final Attribute<?, ?> a = mock(Attribute.class);
    final AnnotatedElement e = mock(AnnotatedElement.class, Mockito.withSettings().extraInterfaces(Member.class));
    final EdmGeospatial g = mock(EdmGeospatial.class);

    when(a.getJavaMember()).thenReturn((Member) e);
    when(e.getAnnotation(EdmGeospatial.class)).thenReturn(g);
    when(g.dimension()).thenReturn(dimension);

    return a;
  }

  @ParameterizedTest
  @MethodSource("mappingJavaGeographyToOData")
  public void checkConvertJavaGeographyToOData(final Class<?> javaType, final Attribute<?, ?> jpaAttribute,
      final EdmPrimitiveTypeKind ODataType) throws ODataJPAModelException {

    assertEquals(ODataType, JPATypeConvertor.convertToEdmSimpleType(javaType, jpaAttribute));
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> JPATypeConvertor.convertToEdmSimpleType(javaType, buildTimeAttribute(TemporalType.TIME)));
    assertEquals(ODataJPAModelException.MessageKeys.TYPE_NOT_SUPPORTED.getKey(), act.getId());
  }

  @ParameterizedTest
  @MethodSource("mappingJavaGeometryToOData")
  public void checkConvertJavaGemetryToOData(final Class<?> javaType, final Attribute<?, ?> jpaAttribute,
      final EdmPrimitiveTypeKind ODataType) throws ODataJPAModelException {

    assertEquals(ODataType, JPATypeConvertor.convertToEdmSimpleType(javaType, jpaAttribute));
    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> JPATypeConvertor.convertToEdmSimpleType(javaType, buildTimeAttribute(TemporalType.TIME)));
    assertEquals(ODataJPAModelException.MessageKeys.TYPE_NOT_SUPPORTED.getKey(), act.getId());
  }

  @ParameterizedTest
  @MethodSource("mappingJavaTimeToOData")
  public void checkConvertJavaDateTimeToOData(final Class<?> javaType, final Attribute<?, ?> jpaAttribute,
      final EdmPrimitiveTypeKind ODataType) throws ODataJPAModelException {

    assertEquals(ODataType, JPATypeConvertor.convertToEdmSimpleType(javaType, jpaAttribute));
  }

  @ParameterizedTest
  @MethodSource("mappingJavaLobToOData")
  public void checkConvertJavaLobToOData(final Class<?> javaType, final Attribute<?, ?> jpaAttribute,
      final EdmPrimitiveTypeKind ODataType) throws ODataJPAModelException {

    assertEquals(ODataType, JPATypeConvertor.convertToEdmSimpleType(javaType, jpaAttribute));

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> JPATypeConvertor.convertToEdmSimpleType(javaType, buildTimeAttribute(TemporalType.TIME)));
    assertEquals(ODataJPAModelException.MessageKeys.TYPE_NOT_SUPPORTED.getKey(), act.getId());
  }

  @ParameterizedTest
  @MethodSource("mappingSimpleJavaToOData")
  public void checkConvertSimpleJavaToOData(final Class<?> javaType, final EdmPrimitiveTypeKind ODataType)
      throws ODataJPAModelException {

    assertEquals(ODataType, JPATypeConvertor.convertToEdmSimpleType(javaType));
  }

  @ParameterizedTest
  @MethodSource("scalarJavaTypes")
  public void checkIsScalarJavaType(final Class<?> javaType, final Attribute<?, ?> jpaAttribute,
      final boolean isSalar) {

    assertEquals(isSalar, JPATypeConvertor.isScalarType(javaType));
  }

  @ParameterizedTest
  @MethodSource("simpleJavaTypes")
  public void checkIsSimpleJavaType(final Class<?> javaType, final Attribute<?, ?> jpaAttribute,
      final boolean isSimple) {

    assertEquals(isSimple, JPATypeConvertor.isSimpleType(javaType, jpaAttribute));
  }

  @ParameterizedTest
  @MethodSource("supportedByOlingo")
  public void checkIsSupportedByOling(final Class<?> javaType, final boolean isSupported) {

    assertEquals(isSupported, JPATypeConvertor.isSupportedByOlingo(javaType));
  }

  @Test
  public void checkReturnsNullOnUnknownTypeWithoutAnnotation() throws ODataJPAModelException {

    assertNull(JPATypeConvertor.convertToEdmSimpleType(BigInteger.class));
  }

  @Test
  public void checkThrowsExceptionOnUnknownTypeWithAnnotation() throws ODataJPAModelException {

    assertThrows(ODataJPAModelException.class,
        () -> JPATypeConvertor.convertToEdmSimpleType(BigInteger.class, buildTimeAttribute(TemporalType.TIME)));
  }

  @Test
  public void checkThrowsExceptionOnUnknownGeographyType() throws ODataJPAModelException {

    assertThrows(ODataJPAModelException.class,
        () -> JPATypeConvertor.convertToEdmSimpleType(BigInteger.class, buildDimensionAttribute(Dimension.GEOGRAPHY)));
  }

  @Test
  public void checkThrowsExceptionOnUnknownGeometryType() throws ODataJPAModelException {

    assertThrows(ODataJPAModelException.class,
        () -> JPATypeConvertor.convertToEdmSimpleType(BigInteger.class, buildDimensionAttribute(Dimension.GEOMETRY)));
  }
}
