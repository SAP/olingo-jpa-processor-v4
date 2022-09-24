package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.UUID;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Default converter to convert from {@link java.util.UUID} to a byte array.
 *
 * @author Oliver Grande
 */
@Converter(autoApply = false)
public class UUIDToStringConverter implements AttributeConverter<UUID, String> {

  @Override
  public String convertToDatabaseColumn(UUID uuid) {
    return uuid == null ? null : uuid.toString();
  }

  @Override
  public UUID convertToEntityAttribute(String dbData) {
    return dbData == null ? null : UUID.fromString(dbData);
  }
}
