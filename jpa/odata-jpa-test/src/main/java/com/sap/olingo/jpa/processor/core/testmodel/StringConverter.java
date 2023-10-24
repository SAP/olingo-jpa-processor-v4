package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class StringConverter implements AttributeConverter<String, String> {

  @Override
  public String convertToDatabaseColumn(final String entityString) {
    return entityString;
  }

  @Override
  public String convertToEntityAttribute(final String dbString) {
    return dbString;
  }

}
