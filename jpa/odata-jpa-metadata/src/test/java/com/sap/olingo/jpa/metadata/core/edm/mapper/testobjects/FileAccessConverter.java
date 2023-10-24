package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.AttributeConverter;

public class FileAccessConverter implements AttributeConverter<FileAccess[], Short> {

  @Override
  public Short convertToDatabaseColumn(final FileAccess[] attributes) {
    return attributes[0].getValue();
  }

  @Override
  public FileAccess[] convertToEntityAttribute(final Short dbData) {
    if (dbData == null)
      return null;
    final List<FileAccess> accesses = new ArrayList<>();
    for (final FileAccess e : Arrays.asList(FileAccess.values())) {
      if (e.getValue() == dbData)
        accesses.add(e);
    }
    return accesses.toArray(new FileAccess[] {});
  }

}
