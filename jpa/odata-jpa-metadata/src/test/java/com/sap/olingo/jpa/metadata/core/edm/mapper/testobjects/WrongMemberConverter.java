package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.util.Arrays;

import javax.persistence.AttributeConverter;

public class WrongMemberConverter implements AttributeConverter<WrongMember, Integer> {

  @Override
  public Integer convertToDatabaseColumn(WrongMember attribute) {
    return attribute.getValue();
  }

  @Override
  public WrongMember convertToEntityAttribute(Integer dbData) {
    for (WrongMember e : Arrays.asList(WrongMember.values())) {
      if (e.getValue() == dbData)
        return e;
    }
    return null;
  }

}
