package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

//This converter has to be mentioned at all columns it is applicable
@Converter(autoApply = false)
public class ByteConverter implements AttributeConverter<String, byte[]> {

  @Override
  public byte[] convertToDatabaseColumn(final String entityString) {
    return entityString.getBytes();
  }

  @Override
  public String convertToEntityAttribute(final byte[] dbString) {
    return new String(dbString);
  }

}
