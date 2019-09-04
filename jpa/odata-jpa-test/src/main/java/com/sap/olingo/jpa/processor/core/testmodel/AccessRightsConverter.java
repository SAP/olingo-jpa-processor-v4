package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class AccessRightsConverter implements AttributeConverter<AccessRights[], Short> {

  @Override
  public Short convertToDatabaseColumn(AccessRights[] attributes) {
    if (attributes == null || attributes.length == 0)
      return null;
    short value = 0;
    for (AccessRights attribute : attributes)
      if (attribute != null)
        value += attribute.getValue();
    return value;
  }

  @Override
  public AccessRights[] convertToEntityAttribute(Short dbData) {
    if (dbData == null)
      return null; // NOSONAR
    final List<AccessRights> accesses = new ArrayList<>();
    for (AccessRights e : Arrays.asList(AccessRights.values())) {
      if ((e.getValue() & dbData) != 0)
        accesses.add(e);
    }
    return accesses.toArray(new AccessRights[] {});
  }

}
