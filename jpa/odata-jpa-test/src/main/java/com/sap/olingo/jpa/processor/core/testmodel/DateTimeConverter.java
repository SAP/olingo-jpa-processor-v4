package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class DateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

  @Override
  public Timestamp convertToDatabaseColumn(final LocalDateTime locDateTime) {
    return (locDateTime == null ? null : Timestamp.from(locDateTime.toInstant(ZoneOffset.UTC)));
  }

  @Override
  public LocalDateTime convertToEntityAttribute(final Timestamp sqlTimestamp) {
    return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDateTime());
  }

}
