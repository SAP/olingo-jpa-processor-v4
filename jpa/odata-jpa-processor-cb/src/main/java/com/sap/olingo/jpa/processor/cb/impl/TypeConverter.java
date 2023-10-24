package com.sap.olingo.jpa.processor.cb.impl;

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
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class TypeConverter {
  private static final Log LOGGER = LogFactory.getLog(TypeConverter.class);

  private TypeConverter() {}

  static Object convert(final Object source, final Class<?> target) { // NOSONAR
    if (target == null || source == null)
      return source;
    if (!target.isAssignableFrom(source.getClass())) {
      if (target == String.class) {
        return source.toString();
      }
      if (boxed(target) == Boolean.class && source instanceof final String asString)
        return Boolean.valueOf(asString);
      if (boxed(target) == Boolean.class && source instanceof final Number asNumber)
        return asNumber.intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
      if (source instanceof final Number asNumber && Number.class.isAssignableFrom(boxed(target))) {
        return convertNumber(asNumber, target);
      }
      if (Temporal.class.isAssignableFrom(target)) {
        return convertTemporal(source, target);
      }
      if (boxed(target) == Character.class && source instanceof final String asString) {
        return convertToCharacter(asString);
      }
      if (target == Duration.class) {
        return convertDuration(source);
      } else {
        LOGGER.debug("No converter found to convert " + source.getClass().getSimpleName() + " to " + target
            .getSimpleName());
        throw new IllegalArgumentException(createCastException(source, target));
      }
    }
    return target.cast(source);

  }

  private static Character convertToCharacter(final String source) {
    if (source.length() > 1) {
      LOGGER.debug("Implicit conversion to Character from String only supported if String not longer than 1");
      throw new IllegalArgumentException("String to long");
    }
    if (source.length() == 0)
      return ' ';
    return source.charAt(0);
  }

  private static Duration convertDuration(final Object source) {
    if (boxed(source.getClass()) == Long.class)
      return Duration.ofSeconds((long) source);
    if (source.getClass() == String.class)
      return Duration.parse((String) source);
    LOGGER.debug("No converter found to convert " + source.getClass().getSimpleName() + " to Duration");
    throw new IllegalArgumentException(createCastException(source, Duration.class));
  }

  private static Temporal convertTemporal(final Object source, final Class<?> target) { // NOSONAR
    if (temporalConversionAllowed(source.getClass(), target)) {
      try {
        if (target == Instant.class) {
          return convertTemporalToInstant(source, target);
        }
        if (target == LocalDate.class) {
          return convertTemporalToLocalDate(source, target);
        }
        if (target == LocalDateTime.class) {
          return convertTemporalToLocalDateTime(source, target);
        }
        if (target == LocalTime.class) {
          return convertTemporalToLocalTime(source, target);
        }
        if (target == OffsetTime.class) {
          return OffsetTime.parse((String) source);
        }
        if (target == OffsetDateTime.class) {
          return convertTemporalToOffsetDateTime(source, target);
        }
      } catch (final DateTimeParseException e) {
        throw new IllegalArgumentException(e);
      }
    }
    throw new IllegalArgumentException(createCastException(source, target));
  }

  private static LocalTime convertTemporalToLocalTime(final Object source, final Class<?> target) {
    if (source.getClass() == Time.class)
      return ((Time) source).toLocalTime();
    if (source.getClass() == String.class)
      return LocalTime.parse((String) source);
    throw new IllegalArgumentException(createCastException(source, target));
  }

  private static LocalDateTime convertTemporalToLocalDateTime(final Object source, final Class<?> target) {
    if (source.getClass() == Timestamp.class)
      return ((Timestamp) source).toLocalDateTime();
    if (source.getClass() == String.class)
      return LocalDateTime.parse((String) source);
    throw new IllegalArgumentException(createCastException(source, target));
  }

  private static LocalDate convertTemporalToLocalDate(final Object source, final Class<?> target) {
    if (source.getClass() == Date.class)
      return ((Date) source).toLocalDate();
    if (source.getClass() == Timestamp.class)
      return ((Timestamp) source).toLocalDateTime().toLocalDate();
    if (source.getClass() == String.class)
      return LocalDate.parse((String) source);
    throw new IllegalArgumentException(createCastException(source, target));
  }

  private static Instant convertTemporalToInstant(final Object source, final Class<?> target) {
    if (source.getClass() == Long.class)
      return Instant.ofEpochMilli(((Number) source).longValue());
    if (source.getClass() == String.class)
      return Instant.parse((String) source);
    if (source.getClass() == Timestamp.class)
      return ((Timestamp) source).toInstant();
    throw new IllegalArgumentException(createCastException(source, target));
  }

  private static OffsetDateTime convertTemporalToOffsetDateTime(final Object source, final Class<?> target) {
    if (source.getClass() == Timestamp.class) {
      return OffsetDateTime.ofInstant(((Timestamp) source).toInstant(), ZoneId.of("UTC"));
    }
    if (source.getClass() == String.class)
      return OffsetDateTime.parse((String) source);
    throw new IllegalArgumentException(createCastException(source, target));
  }

  public static Class<?> boxed(final Class<?> javaType) {
    if (javaType == int.class) return Integer.class;
    if (javaType == long.class) return Long.class;
    if (javaType == boolean.class) return Boolean.class;
    if (javaType == byte.class) return Byte.class;
    if (javaType == char.class) return Character.class;
    if (javaType == float.class) return Float.class;
    if (javaType == short.class) return Short.class;
    if (javaType == double.class) return Double.class;
    return javaType;
  }

  private static Object convertNumber(final Number source, final Class<?> target) { // NOSONAR
    if (numberConversionAllowed(source.getClass(), target)) {
      try {
        if (boxed(target) == Long.class) {
          return source.longValue();
        }
        if (boxed(target) == Integer.class) {
          return source.intValue();
        }
        if (boxed(target) == Short.class) {
          return source.shortValue();
        }
        if (boxed(target) == Byte.class) {
          return source.byteValue();
        }
        if (boxed(target) == Float.class) {
          return source.floatValue();
        }
        if (boxed(target) == Double.class) {
          return source.doubleValue();
        }
        if (target == BigInteger.class) {
          return BigInteger.valueOf(source.longValue());
        }
        if (target == BigDecimal.class) {
          return convertToBigDecimal(source);
        }
      } catch (final NumberFormatException e) {
        throw new IllegalArgumentException(e);
      }
    }
    throw new IllegalArgumentException(createCastException(source, target));
  }

  private static Object convertToBigDecimal(final Number result) {
    if (result instanceof Float || result instanceof Double) {
      final Double d = result.doubleValue();
      if (Double.isInfinite(d))
        throw new IllegalArgumentException(
            String.format("Type cast error: Can't convert infinity value of type '%s' to BigDecimal. @Convert required",
                result));
      if (Double.isNaN(d))
        throw new IllegalArgumentException(
            "Type cast error: Can't convert 'Not A Number' to BigDecimal. @Convert required");
      return BigDecimal.valueOf(result.doubleValue());
    }
    return BigDecimal.valueOf(result.longValue());
  }

  private static String createCastException(final Object result, final Class<?> target) {
    return String.format("Type cast error: Can't convert '%s' to '%s'. @Convert required",
        result.getClass(), target);
  }

  private static boolean numberConversionAllowed(final Class<?> source, final Class<?> target) { // NOSONAR
    if (target == Short.class || target == short.class) return source == Byte.class || source == Integer.class;
    if (target == Integer.class || target == int.class) return source == Byte.class || source == Short.class
        || source == Long.class;
    if (target == Long.class || target == long.class) return source == Byte.class || source == Short.class
        || source == Integer.class || source == BigInteger.class || source == BigDecimal.class;
    if (target == BigInteger.class) return source == Byte.class || source == Short.class || source == Integer.class
        || source == Long.class || source == BigDecimal.class;
    if (target == Float.class || target == float.class) return source == Byte.class || source == Short.class
        || source == Integer.class || source == Long.class || source == BigInteger.class || source == BigDecimal.class;
    if (target == Double.class || target == double.class) return source == Byte.class || source == Short.class
        || source == Integer.class || source == Long.class || source == BigInteger.class || source == BigDecimal.class
        || source == Float.class;
    if (target == BigDecimal.class) return source == Byte.class || source == Short.class || source == Integer.class
        || source == Long.class || source == BigInteger.class || source == Float.class || source == Double.class;
    return false;
  }

  private static boolean temporalConversionAllowed(final Class<?> source, final Class<?> target) { // NOSONAR
    if (target == Instant.class) return source == Long.class || source == String.class || source == Timestamp.class;
    if (target == LocalDate.class) return source == Date.class || source == Timestamp.class || source == String.class;
    if (target == LocalDateTime.class) return source == Timestamp.class || source == String.class;
    if (target == LocalTime.class) return source == Time.class || source == String.class;
    if (target == OffsetDateTime.class) return source == Timestamp.class || source == String.class;
    if (target == OffsetTime.class) return source == String.class;
    return false;
  }
}
