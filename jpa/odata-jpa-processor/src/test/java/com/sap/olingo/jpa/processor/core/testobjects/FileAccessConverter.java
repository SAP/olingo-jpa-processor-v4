package com.sap.olingo.jpa.processor.core.testobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.AttributeConverter;

public class FileAccessConverter implements AttributeConverter<FileAccess[], Short> {

  @Override
  public Short convertToDatabaseColumn(FileAccess[] attribute) {
    return attribute[0].getValue();
  }

  @Override
  public FileAccess[] convertToEntityAttribute(Short dbData) {
    if (dbData == null)
      return null;
    final List<FileAccess> accesses = new ArrayList<>();
    for (FileAccess e : Arrays.asList(FileAccess.values())) {
      if (e.getValue() == dbData)
        accesses.add(e);
    }
    return accesses.toArray(new FileAccess[] {});
  }

}
