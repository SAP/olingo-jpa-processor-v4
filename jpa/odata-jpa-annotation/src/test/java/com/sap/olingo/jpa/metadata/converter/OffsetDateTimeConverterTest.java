/**
 *
 */
package com.sap.olingo.jpa.metadata.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * @since 1.0.7
 * 18.01.2022
 */
class OffsetDateTimeConverterTest {
  private OffsetDateTimeConverter cut;

  @BeforeEach
  void setup() {
    cut = new OffsetDateTimeConverter();
  }

  @Test
  void testConvertToDBNull() {
    assertNull(cut.convertToDatabaseColumn(null));
  }

  @Test
  void testConvertToDB() {
    final ZonedDateTime zdt = ZonedDateTime
        .of(1972, 12, 6, 12, 17, 7, 0, ZoneId.of(ZoneId.SHORT_IDS.get("HST")));
    final OffsetDateTime act = cut.convertToDatabaseColumn(zdt);
    assertEquals(1972, act.getYear());
    assertEquals(-10 * 3600, act.getOffset().getTotalSeconds());
  }

  @Test
  void testConvertToEntityNull() {
    assertNull(cut.convertToEntityAttribute(null));
  }

  @Test
  void testConvertToEntity() {
    final OffsetDateTime odt = OffsetDateTime
        .of(1972, 12, 6, 12, 17, 7, 0, ZoneOffset.ofHours(-10));
    final ZonedDateTime act = cut.convertToEntityAttribute(odt);
    assertEquals(1972, act.getYear());
    assertEquals("-10:00", act.getZone().getId());
  }

}
