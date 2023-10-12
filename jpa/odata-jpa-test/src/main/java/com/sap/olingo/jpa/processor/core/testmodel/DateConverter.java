package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Date;
import java.time.LocalDate;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class DateConverter implements AttributeConverter<LocalDate, Date> {

  @Override
  public Date convertToDatabaseColumn(final LocalDate locDate) {
    return (locDate == null ? null : Date.valueOf(locDate));
  }

  @Override
  public LocalDate convertToEntityAttribute(final Date sqlDate) {
    return (sqlDate == null ? null : sqlDate.toLocalDate());
  }

}
