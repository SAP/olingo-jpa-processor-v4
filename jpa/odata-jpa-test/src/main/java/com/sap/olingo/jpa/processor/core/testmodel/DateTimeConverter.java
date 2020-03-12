package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class DateTimeConverter implements AttributeConverter<LocalDateTime, Timestamp> {

  @Override
  public Timestamp convertToDatabaseColumn(LocalDateTime locDateTime) {
    return (locDateTime == null ? null : Timestamp.from(locDateTime.toInstant(ZoneOffset.UTC)));
  }

  @Override
  public LocalDateTime convertToEntityAttribute(Timestamp sqlTimestamp) {
    return (sqlTimestamp == null ? null : sqlTimestamp.toLocalDateTime());
  }

}

