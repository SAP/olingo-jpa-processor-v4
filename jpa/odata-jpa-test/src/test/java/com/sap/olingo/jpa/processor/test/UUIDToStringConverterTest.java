package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.UUIDToStringConverter;

final class UUIDToStringConverterTest {
  @Test
  void testConversion() {
    final UUIDToStringConverter cut = new UUIDToStringConverter();
    final UUID exp = UUID.randomUUID();

    assertEquals(exp, cut.convertToEntityAttribute(cut.convertToDatabaseColumn(exp)));
  }
}