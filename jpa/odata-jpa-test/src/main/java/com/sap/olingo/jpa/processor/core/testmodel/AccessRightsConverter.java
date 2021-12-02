package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class AccessRightsConverter implements AttributeConverter<AccessRights[], Short> {

  @Override
  public Short convertToDatabaseColumn(final AccessRights[] attributes) {
    if (attributes == null || attributes.length == 0)
      return null;
    short value = 0;
    for (final AccessRights attribute : attributes)
      if (attribute != null)
        value += attribute.getValue();
    return value;
  }

  @Override
  public AccessRights[] convertToEntityAttribute(final Short dbData) {
    if (dbData == null)
      return null; // NOSONAR
    final List<AccessRights> accesses = new ArrayList<>();
    for (final AccessRights e : AccessRights.values()) {
      if ((e.getValue() & dbData.shortValue()) != 0)
        accesses.add(e);
    }
    return accesses.toArray(new AccessRights[] {});
  }
}
