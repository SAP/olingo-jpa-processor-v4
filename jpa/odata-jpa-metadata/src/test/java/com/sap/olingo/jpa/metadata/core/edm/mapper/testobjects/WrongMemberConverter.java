package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.AttributeConverter;

public class WrongMemberConverter implements AttributeConverter<WrongMember[], Integer> {

  @Override
  public Integer convertToDatabaseColumn(final WrongMember[] attributes) {
    return attributes[0].getValue();
  }

  @Override
  public WrongMember[] convertToEntityAttribute(final Integer dbData) {
    if (dbData == null)
      return null;
    final List<WrongMember> accesses = new ArrayList<>();
    for (final WrongMember e : Arrays.asList(WrongMember.values())) {
      if (e.getValue() == dbData)
        accesses.add(e);
    }
    return accesses.toArray(new WrongMember[] {});
  }

}
