package com.sap.olingo.jpa.processor.cb.impl;

import static com.sap.olingo.jpa.processor.cb.impl.TypeConverter.convert;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TypeConverterTest {

  static Stream<Arguments> numericConversion() {
    return Stream.of(
        arguments(new Short((short) 5), new Byte("5"), Short.class),

        arguments(new Integer(5), new Byte("5"), Integer.class),
        arguments(new Integer(5), new Short((short) 5), Integer.class),

        arguments(new Long(5), new Byte("5"), Long.class),
        arguments(new Long(5), new Short((short) 5), Long.class),
        arguments(new Long(5), new Integer(5), Long.class),

        arguments(new Float(5), new Byte("5"), Float.class),
        arguments(new Float(5), new Short((short) 5), Float.class),
        arguments(new Float(5), new Integer(5), Float.class),
        arguments(new Float(5), new Long(5), Float.class),
        arguments(new Float(10), BigInteger.TEN, Float.class),
        arguments(new Float(10), BigDecimal.TEN, Float.class),

        arguments(new Double(5), new Byte("5"), Double.class),
        arguments(new Double(5), new Short((short) 5), Double.class),
        arguments(new Double(5), new Integer(5), Double.class),
        arguments(new Double(5), new Long(5), Double.class),
        arguments(new Double(5), new Float(5), Double.class),
        arguments(new Double(10), BigInteger.TEN, Double.class),
        arguments(new Double(10), BigDecimal.TEN, Double.class),

        arguments(BigInteger.TEN, new Byte("10"), BigInteger.class),
        arguments(BigInteger.TEN, new Short((short) 10), BigInteger.class),
        arguments(BigInteger.TEN, new Integer(10), BigInteger.class),
        arguments(BigInteger.TEN, new Long(10), BigInteger.class),

        arguments(BigDecimal.TEN, new Byte("10"), BigDecimal.class),
        arguments(BigDecimal.TEN, new Short((short) 10), BigDecimal.class),
        arguments(BigDecimal.TEN, new Integer(10), BigDecimal.class),
        arguments(BigDecimal.TEN, new Long(10), BigDecimal.class),
        arguments(BigDecimal.valueOf(new Double(10)), new Float(10), BigDecimal.class),
        arguments(BigDecimal.valueOf(new Double(10)), new Double(10), BigDecimal.class),
        arguments(BigDecimal.TEN, BigInteger.TEN, BigDecimal.class),

        arguments(new Short((short) 10), new Byte("10"), short.class),
        arguments(new Integer(10), new Byte("10"), int.class),
        arguments(new Long(5), new Byte("5"), long.class),
        arguments(new Float(5), new Byte("5"), float.class),
        arguments(new Double(10), new Byte("10"), double.class));
  }

  static Stream<Arguments> numericConversionNotSupported() {
    return Stream.of(

        arguments(new Long(5), Short.class),
        arguments(new Integer(5), Short.class),
        arguments(new Float(5), Short.class),
        arguments(new Double(5), Short.class),
        arguments(BigInteger.TEN, Short.class),
        arguments(BigDecimal.TEN, Short.class),

        // arguments(new Long(5), Integer.class),
        arguments(new Float(5), Integer.class),
        arguments(new Double(5), Integer.class),
        arguments(BigInteger.TEN, Integer.class),
        arguments(BigDecimal.TEN, Integer.class),

        arguments(new Float(5.3), Long.class),
        arguments(new Double(5), Long.class),
        arguments(BigInteger.TEN, Long.class),
        arguments(BigDecimal.TEN, Long.class),

        arguments(new Double(5), Float.class),

        arguments(new Float(10), BigInteger.class),
        arguments(new Double(10), BigInteger.class),
        arguments(BigDecimal.TEN, BigInteger.class));
  }

  static Stream<Arguments> infinityValueConversion() {
    return Stream.of(

        arguments(Double.POSITIVE_INFINITY, BigDecimal.class),
        arguments(Double.NEGATIVE_INFINITY, BigDecimal.class),
        arguments(Float.POSITIVE_INFINITY, BigDecimal.class),
        arguments(Float.NEGATIVE_INFINITY, BigDecimal.class));
  }

  static Stream<Arguments> nanValueConversion() {
    return Stream.of(
        arguments(Double.NaN, BigDecimal.class),
        arguments(Float.NaN, BigDecimal.class));
  }

  static Stream<Arguments> booleanConversion() {
    return Stream.of(
        arguments(Boolean.TRUE, "true"),
        arguments(Boolean.TRUE, "TrUe"),
        arguments(Boolean.FALSE, "false"),
        arguments(Boolean.FALSE, "Test"),
        arguments(Boolean.TRUE, 10),
        arguments(Boolean.TRUE, 5L),
        arguments(Boolean.TRUE, 5L),
        arguments(Boolean.TRUE, 5.5F),
        arguments(Boolean.FALSE, 0),
        arguments(Boolean.FALSE, BigInteger.ZERO));
  }

  static Stream<Arguments> temporalConversion() {
    return Stream.of(
        arguments(Instant.parse("2007-12-03T10:15:30.05Z"), new Long(1196676930050L), Instant.class),
        arguments(Instant.parse("2007-12-03T10:15:30.05Z"), "2007-12-03T10:15:30.05Z", Instant.class),
        arguments(Instant.parse("2007-12-03T10:15:30.05Z").minusMillis(TimeZone.getDefault().getRawOffset()), Timestamp
            .valueOf("2007-12-03 10:15:30.05"), Instant.class),
        arguments(LocalDate.parse("2007-12-03"), Date.valueOf("2007-12-03"), LocalDate.class),
        arguments(LocalDate.parse("2007-12-03"), Timestamp.valueOf("2007-12-03 00:00:00"), LocalDate.class),
        arguments(LocalDate.parse("2007-12-03"), "2007-12-03", LocalDate.class),
        // arguments(Instant.parse("2007-12-03T10:15:30.05Z"), "2007-12-03T10:15:30.05Z", Time.class),
        arguments(LocalTime.parse("10:15:30"), Time.valueOf("10:15:30"), LocalTime.class),
        arguments(LocalTime.parse("10:15:30"), "10:15:30", LocalTime.class),
        arguments(LocalDateTime.parse("2007-12-03T10:15:30.05"), Timestamp.valueOf("2007-12-03 10:15:30.05"),
            LocalDateTime.class),
        arguments(LocalDateTime.parse("2007-12-03T10:15:30.05"), "2007-12-03T10:15:30.05", LocalDateTime.class),
        arguments(OffsetTime.parse("10:15:30+01:00"), "10:15:30+01:00", OffsetTime.class),
        arguments(OffsetDateTime.parse("2007-12-03T10:15:30+01:00"), "2007-12-03T10:15:30+01:00",
            OffsetDateTime.class));
  }

  @Test
  void testToString() {
    assertEquals("123456789", convert(new Integer(123456789), String.class));
  }

  @ParameterizedTest
  @MethodSource("numericConversion")
  void testConvertNumeric(final Object exp, final Object source, final Class<?> targetType) {
    assertEquals(exp, convert(source, targetType));
  }

  @ParameterizedTest
  @MethodSource("numericConversionNotSupported")
  void testConvertNumericThrowsExceptionOnUnsupported(final Object source, final Class<?> targetType) {

    final IllegalArgumentException act = assertThrows(IllegalArgumentException.class,
        () -> convert(source, targetType));
    assertTrue(act.getMessage().contains("@Convert required"));
  }

  @ParameterizedTest
  @MethodSource("infinityValueConversion")
  void testConvertNumericInfinityValue(final Object source, final Class<?> targetType) {

    final IllegalArgumentException act = assertThrows(IllegalArgumentException.class,
        () -> convert(source, targetType));
    assertTrue(act.getMessage().contains("infinity value"));
  }

  @ParameterizedTest
  @MethodSource("nanValueConversion")
  void testConvertNumericNaN(final Object source, final Class<?> targetType) {

    final IllegalArgumentException act = assertThrows(IllegalArgumentException.class,
        () -> convert(source, targetType));
    assertTrue(act.getMessage().contains("'Not A Number'"));
  }

  @ParameterizedTest
  @MethodSource("booleanConversion")
  void testConvertBoolean(final Object exp, final Object source) {

    assertEquals(exp, convert(source, Boolean.class));
  }

  @ParameterizedTest
  @MethodSource("temporalConversion")
  void testConvertTemporal(final Object exp, final Object source, final Class<?> targetType) {

    assertEquals(exp, convert(source, targetType));
  }

  @Test
  void testConvertNumericThrowsExceptionWrongString() {

    assertThrows(IllegalArgumentException.class, () -> convert("Test", Integer.class));
  }

  @Test
  void testConvertTemporalThrowsExceptionWrongString() {

    assertThrows(IllegalArgumentException.class, () -> convert("Test", LocalTime.class));
  }

  @Test
  void testConvertTemporalThrowsExceptionOnUnsupportwed() {
    final Timestamp timestamp = Timestamp.valueOf("2007-12-03 00:00:00");
    assertThrows(IllegalArgumentException.class, () -> convert(timestamp, ZonedDateTime.class));
  }
}
