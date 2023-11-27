package com.sap.olingo.jpa.processor.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.persistence.AttributeConverter;

import org.junit.jupiter.api.Test;

abstract class AbstractConverterTest<E, D> {
  protected AttributeConverter<E, D> cut;
  protected E exp;

  @Test
  void testConversion() {
    assertEquals(exp, cut.convertToEntityAttribute(cut.convertToDatabaseColumn(exp)));
  }

  @Test
  void testToDatabaseReturnsNullOnNull() {
    assertNull(cut.convertToDatabaseColumn(null));
  }

  @Test
  void testToEntityAttributeReturnsNullOnNull() {
    assertNull(cut.convertToEntityAttribute(null));
  }

}