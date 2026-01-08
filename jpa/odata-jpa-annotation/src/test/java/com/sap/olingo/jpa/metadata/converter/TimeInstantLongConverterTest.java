/**
 *
 */
package com.sap.olingo.jpa.metadata.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * @since 1.0.9
 * 18.01.2022
 */
class TimeInstantLongConverterTest {
  private static final long EPOCH_MILLIS = 1000000;
  private TimeInstantLongConverter cut;

  @BeforeEach
  void setup() {
    cut = new TimeInstantLongConverter();
  }

  @Test
  void testConervertToDBNull() {
    assertNull(cut.convertToDatabaseColumn(null));
  }

  @Test
  void testConervertToDB() {
    final Instant inst = Instant.ofEpochMilli(EPOCH_MILLIS);
    final Number act = cut.convertToDatabaseColumn(inst);
    assertEquals(EPOCH_MILLIS, act);
  }

  @Test
  void testConervertToEntityNull() {
    assertNull(cut.convertToEntityAttribute(null));
  }

  @Test
  void testConervertToEntity() {
    final Instant act = cut.convertToEntityAttribute(EPOCH_MILLIS);
    assertEquals(EPOCH_MILLIS / 1000, act.getEpochSecond());
  }
}
