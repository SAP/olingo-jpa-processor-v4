package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;
import java.time.Instant;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Default converter to convert from {@link Long} to {@link java.sql.Timestamp}.
 *
 * @author Oliver Grande
 * @version 1.0.0-RC
 */
@Converter(autoApply = false)
public class TimestampLongConverter implements AttributeConverter<Timestamp, Long> {

  @Override
  public Long convertToDatabaseColumn(final Timestamp instant) {
    return instant == null ? null : instant.toInstant().toEpochMilli();
  }

  @Override
  public Timestamp convertToEntityAttribute(final Long epochMilliseconds) {
    return epochMilliseconds == null ? null : Timestamp.from(Instant.ofEpochMilli(epochMilliseconds));
  }
}