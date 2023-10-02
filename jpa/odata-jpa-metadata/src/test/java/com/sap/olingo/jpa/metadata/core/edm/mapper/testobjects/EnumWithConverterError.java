package com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEnumeration;

@EdmEnumeration(converter = ConverterWithConstructorError.class)
public enum EnumWithConverterError {
  TEST, DUMMY;
}
