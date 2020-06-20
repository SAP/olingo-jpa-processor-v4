package com.sap.olingo.jpa.metadata.converter;

import java.time.Instant;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Default converter to convert from {@link Long} to {@link java.time.Instant}.
 *
 * @author Oliver Grande
 * @since 1.0.0-RC
 */
@Converter(autoApply = false)
public class TimeInstantLongConverter implements AttributeConverter<Instant, Long> {

  @Override
  public Long convertToDatabaseColumn(final Instant instant) {
    return instant == null ? null : instant.toEpochMilli();
  }

  @Override
  public Instant convertToEntityAttribute(final Long epochMillis) {
    return epochMillis == null ? null : Instant.ofEpochMilli(epochMillis);
  }
}
