package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Arrays;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = false)
public class AccessRightsConverter implements AttributeConverter<AccessRights, Short> {

  @Override
  public Short convertToDatabaseColumn(AccessRights attribute) {
    return attribute != null ? attribute.getValue() : null;
  }

  @Override
  public AccessRights convertToEntityAttribute(Short dbData) {
    if (dbData != null) {
      for (AccessRights e : Arrays.asList(AccessRights.values())) {
        if (e.getValue() == dbData)
          return e;
      }
    }
    return null;
  }

}
