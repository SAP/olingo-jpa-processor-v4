package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class ByteConverter implements AttributeConverter<String, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(String entityString) {
    return entityString.getBytes();
  }

  @Override
  public String convertToEntityAttribute(byte[] dbString) {
    return new String(dbString);
  }

}
