package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class StringConverter implements AttributeConverter<String, String> {

  @Override
  public String convertToDatabaseColumn(String entityString) {
    return entityString;
  }

  @Override
  public String convertToEntityAttribute(String dbString) {
    return dbString;
  }

}
