/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.persistence.AttributeConverter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * @since pom_version
 * 18.01.2022
 */
class EdmEnumerationTest {
  private AttributeConverter<Enum<?>[], Integer> cut;

  @BeforeEach
  void setup() {
    cut = new EdmEnumeration.DummyConverter();
  }

  @Test
  void testConvertToDatabaseColumnReturnsNull() {
    final EdmTopLevelElementRepresentation[] data = {};
    assertNull(cut.convertToDatabaseColumn(data));
  }

  @Test
  void testConvertToDatabaseColumnReturnsOrdinal() {
    final EdmFunctionType[] data = { EdmFunctionType.JavaClass };
    assertEquals(1, cut.convertToDatabaseColumn(data));
  }

  @Test
  void testConvertToDatabaseColumnReturnsOrdinalOfFirst() {
    final EdmTopLevelElementRepresentation[] data = { EdmTopLevelElementRepresentation.AS_SINGLETON,
        EdmTopLevelElementRepresentation.AS_ENTITY_SET };
    assertEquals(3, cut.convertToDatabaseColumn(data));
  }

  @Test
  void testConvertToDatabaseColumnReturnsNullOnEmpty() {
    assertNull(cut.convertToDatabaseColumn(null));
  }

  @Test
  void testConvertToEntityAttributeReturnsFirst() {
    assertNull(cut.convertToEntityAttribute(10));
  }
}
