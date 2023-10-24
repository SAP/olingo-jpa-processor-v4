package com.sap.olingo.jpa.metadata.converter;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Default converter to convert from {@link java.time.OffsetDateTime} to {@link java.time.ZonedDateTime}. This is
 * required, as Olingo 4.7.1 only supports ZonedDateTime, where as JPA 2.2 supports OffsetDateTime.
 * @author Oliver Grande
 * Created: 09.03.2020
 *
 */
@Converter(autoApply = false)
public class OffsetDateTimeConverter implements AttributeConverter<ZonedDateTime, OffsetDateTime> {

  @Override
  public OffsetDateTime convertToDatabaseColumn(final ZonedDateTime zonedDateTime) {
    return zonedDateTime == null ? null : zonedDateTime.toOffsetDateTime();
  }

  @Override
  public ZonedDateTime convertToEntityAttribute(final OffsetDateTime dateTimeWithOffset) {
    return dateTimeWithOffset == null ? null : dateTimeWithOffset.toZonedDateTime();
  }

}
