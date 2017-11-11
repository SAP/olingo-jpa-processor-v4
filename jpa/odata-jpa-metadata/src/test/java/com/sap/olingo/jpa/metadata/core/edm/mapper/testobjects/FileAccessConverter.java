package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.util.Arrays;

import javax.persistence.AttributeConverter;

public class FileAccessConverter implements AttributeConverter<FileAccess, Short> {

  @Override
  public Short convertToDatabaseColumn(FileAccess attribute) {
    return attribute.getValue();
  }

  @Override
  public FileAccess convertToEntityAttribute(Short dbData) {
    for (FileAccess e : Arrays.asList(FileAccess.values())) {
      if (e.getValue() == dbData)
        return e;
    }
    return null;
  }

}
