package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import jakarta.persistence.AttributeConverter;

public class ConverterWithConstructorError implements AttributeConverter<Enum<?>[], Integer> {

  private final int counter;

  public ConverterWithConstructorError(final int counter) {
    super();
    this.counter = counter;
  }

  @Override
  public Integer convertToDatabaseColumn(final Enum<?>[] attribute) {
    return null;
  }

  @Override
  public Enum<?>[] convertToEntityAttribute(final Integer dbData) {
    return null;
  }

  public int getCounter() {
    return counter;
  }

}
