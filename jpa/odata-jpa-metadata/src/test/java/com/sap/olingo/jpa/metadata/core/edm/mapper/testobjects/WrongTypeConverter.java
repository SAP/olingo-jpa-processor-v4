package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.math.BigDecimal;

import javax.persistence.AttributeConverter;

public class WrongTypeConverter implements AttributeConverter<WrongType[], BigDecimal> {

  @Override
  public BigDecimal convertToDatabaseColumn(WrongType[] attribute) {
    return WrongType.TEST.getValue();
  }

  @Override
  public WrongType[] convertToEntityAttribute(BigDecimal dbData) {
    return new WrongType[] { WrongType.TEST };
  }

}
