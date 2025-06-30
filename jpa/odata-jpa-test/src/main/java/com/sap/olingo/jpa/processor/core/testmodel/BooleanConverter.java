package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BooleanConverter implements AttributeConverter<Boolean, String> {

  @Override
  public String convertToDatabaseColumn(final Boolean attribute) {
    if (attribute != null)
      return attribute.toString();
    return null;
  }

  @Override
  public Boolean convertToEntityAttribute(final String dbData) {
    if (dbData != null)
      return Boolean.valueOf(dbData);
    return null;
  }

}
