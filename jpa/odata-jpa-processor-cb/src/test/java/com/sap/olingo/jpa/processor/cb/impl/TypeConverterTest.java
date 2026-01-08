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
import java.time.Duration;
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
    return Stream.of( // expected; source;target
        arguments(Byte.valueOf("65"), "A".getBytes()[0], Byte.class),

        arguments(Short.valueOf((short) 5), Byte.valueOf("5"), Short.class),
        arguments(Short.valueOf((short) 5), Integer.valueOf(5), Short.class),

        arguments(Integer.valueOf(5), Byte.valueOf("5"), Integer.class),
        arguments(Integer.valueOf(5), Short.valueOf((short) 5), Integer.class),
        arguments(Integer.valueOf(5), Long.valueOf(5), Integer.class),

        arguments(Long.valueOf(5), Byte.valueOf("5"), Long.class),
        arguments(Long.valueOf(5), Short.valueOf((short) 5), Long.class),
        arguments(Long.valueOf(5), Integer.valueOf(5), Long.class),
        arguments(Long.valueOf(5), BigInteger.valueOf(5), Long.class),
        arguments(Long.valueOf(5), BigDecimal.valueOf(5), Long.class),

        arguments(Float.valueOf(5), Byte.valueOf("5"), Float.class),
        arguments(Float.valueOf(5), Short.valueOf((short) 5), Float.class),
        arguments(Float.valueOf(5), Integer.valueOf(5), Float.class),
        arguments(Float.valueOf(5), Long.valueOf(5), Float.class),
        arguments(Float.valueOf(10), BigInteger.TEN, Float.class),
        arguments(Float.valueOf(10), BigDecimal.TEN, Float.class),

        arguments(Double.valueOf(5), Byte.valueOf("5"), Double.class),
        arguments(Double.valueOf(5), Short.valueOf((short) 5), Double.class),
        arguments(Double.valueOf(5), Integer.valueOf(5), Double.class),
        arguments(Double.valueOf(5), Long.valueOf(5), Double.class),
        arguments(Double.valueOf(5), Float.valueOf(5), Double.class),
        arguments(Double.valueOf(10), BigInteger.TEN, Double.class),
        arguments(Double.valueOf(10), BigDecimal.TEN, Double.class),

        arguments(BigInteger.TEN, Byte.valueOf("10"), BigInteger.class),
        arguments(BigInteger.TEN, Short.valueOf((short) 10), BigInteger.class),
        arguments(BigInteger.TEN, Integer.valueOf(10), BigInteger.class),
        arguments(BigInteger.TEN, Long.valueOf(10), BigInteger.class),
        arguments(BigInteger.TEN, BigDecimal.valueOf(10), BigInteger.class),

        arguments(BigDecimal.TEN, Byte.valueOf("10"), BigDecimal.class),
        arguments(BigDecimal.TEN, Short.valueOf((short) 10), BigDecimal.class),
        arguments(BigDecimal.TEN, Integer.valueOf(10), BigDecimal.class),
        arguments(BigDecimal.TEN, Long.valueOf(10), BigDecimal.class),
        arguments(BigDecimal.valueOf(10.0), Float.valueOf(10), BigDecimal.class),
        arguments(BigDecimal.valueOf(10.0), Double.valueOf(10), BigDecimal.class),
        arguments(BigDecimal.TEN, BigInteger.TEN, BigDecimal.class),

        arguments(Short.valueOf((short) 10), Byte.valueOf("10"), short.class),
        arguments(Integer.valueOf(10), Byte.valueOf("10"), int.class),
        arguments(Long.valueOf(5), Byte.valueOf("5"), long.class),
        arguments(Float.valueOf(5), Byte.valueOf("5"), float.class),
        arguments(Double.valueOf(10), Byte.valueOf("10"), double.class));
  }

  static Stream<Arguments> numericConversionNotSupported() {
    return Stream.of(

        arguments(Long.valueOf(5), Short.class),
        arguments(Float.valueOf(5), Short.class),
        arguments(Double.valueOf(5), Short.class),
        arguments(BigInteger.TEN, Short.class),
        arguments(BigDecimal.TEN, Short.class),

        // arguments(Long.valueOf(5), Integer.class),
        arguments(Float.valueOf(5), Integer.class),
        arguments(Double.valueOf(5), Integer.class),
        arguments(BigInteger.TEN, Integer.class),
        arguments(BigDecimal.TEN, Integer.class),

        arguments(Float.valueOf(5.3F), Long.class),
        arguments(Double.valueOf(5), Long.class),
        arguments(Double.valueOf(5), Float.class),

        arguments(Float.valueOf(10), BigInteger.class),
        arguments(Double.valueOf(10), BigInteger.class));
    // arguments(BigDecimal.TEN, BigInteger.class));
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
        arguments(Instant.parse("2007-12-03T10:15:30.05Z"), Long.valueOf(1196676930050L), Instant.class),
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
            OffsetDateTime.class),
        arguments(Timestamp.valueOf("2007-12-03 10:15:30.05"), LocalDateTime.parse("2007-12-03T10:15:30.05"),
            Timestamp.class),
        arguments(Timestamp.valueOf("2007-12-03 00:00:00"), LocalDate.parse("2007-12-03"),
            Timestamp.class),
        arguments(Timestamp.valueOf("2007-12-03 10:15:30.05"), "2007-12-03 10:15:30.05", Timestamp.class));
  }

  static Stream<Arguments> durationConversion() {
    return Stream.of(
        arguments(Duration.ofHours(3L), Long.valueOf(10800L), Duration.class),
        arguments(Duration.ofHours(3L), "PT3H", Duration.class));
  }

  static Stream<Arguments> temporalConversionNotSupported() {
    return Stream.of(
        arguments(OffsetDateTime.parse("2007-12-03T10:15:30+01:00"), LocalDateTime.class),
        arguments(Integer.valueOf(10), LocalDateTime.class),
        arguments("Test", LocalDateTime.class),
        arguments(Integer.valueOf(10), LocalTime.class));
  }

  @Test
  void testToString() {
    assertEquals("123456789", convert(Integer.valueOf(123456789), String.class));
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

  @ParameterizedTest
  @MethodSource("durationConversion")
  void testConvertDuration(final Object exp, final Object source, final Class<?> targetType) {

    assertEquals(exp, convert(source, targetType));
  }

  @Test
  void testConvertNumericThrowsExceptionWrongString() {

    assertThrows(IllegalArgumentException.class, () -> convert("Test", Integer.class));
  }

  @ParameterizedTest
  @MethodSource("temporalConversionNotSupported")
  void testConvertTemporalThrowsException(final Object source, final Class<?> targetType) {

    assertThrows(IllegalArgumentException.class, () -> convert(source, targetType));
  }

  @Test
  void testConvertTemporalThrowsExceptionOnUnsupported() {
    final Timestamp timestamp = Timestamp.valueOf("2007-12-03 00:00:00");
    assertThrows(IllegalArgumentException.class, () -> convert(timestamp, ZonedDateTime.class));
  }

  @Test
  void testConvertDurationThrowsExceptionOnUnsupported() {
    final Integer i = 10;
    assertThrows(IllegalArgumentException.class, () -> convert(i, Duration.class));
  }

  @Test
  void testConvertStringToCharacter() {
    assertEquals('A', convert("A", Character.class));
  }

  @Test
  void testConvertStringToCharacterEmpty() {
    assertEquals(' ', convert("", Character.class));
  }

  @Test
  void testConvertStringToCharacterThrowsExceptionOnWrongLength() {
    assertThrows(IllegalArgumentException.class, () -> convert("AA", Character.class));
  }
}
