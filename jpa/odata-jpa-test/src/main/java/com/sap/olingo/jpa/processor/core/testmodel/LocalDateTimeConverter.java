package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

  @Override
  public Date convertToDatabaseColumn(final LocalDateTime attribute) {
    if (attribute != null)
      return Date.from(attribute.toInstant(ZoneOffset.UTC));
    return null;
  }

  @Override
  public LocalDateTime convertToEntityAttribute(final Date dbData) {
    if (dbData != null)
      return LocalDateTime.ofInstant(dbData.toInstant(), ZoneId.of("UTC"));
    return null;
  }

}
