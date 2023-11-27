package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.math.BigDecimal;

import jakarta.persistence.AttributeConverter;

public class WrongTypeConverter implements AttributeConverter<WrongType[], BigDecimal> {

  @Override
  public BigDecimal convertToDatabaseColumn(final WrongType[] attribute) {
    return WrongType.TEST.getValue();
  }

  @Override
  public WrongType[] convertToEntityAttribute(final BigDecimal dbData) {
    return new WrongType[] { WrongType.TEST };
  }

}
