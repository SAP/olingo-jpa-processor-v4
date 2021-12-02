/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.converter.OffsetDateTimeConverter;

/**
 * @author Oliver Grande
 * Created: 09.03.2020
 *
 */
class TestOffsetDateTimeConverter {
  private OffsetDateTimeConverter cut;

  @BeforeEach
  void setup() {
    cut = new OffsetDateTimeConverter();
  }

  @Test
  void checkConvertToDatabaseColumnReturnNullOnNull() {
    assertNull(cut.convertToDatabaseColumn(null));
  }

  @Test
  void checkConvertToEntityAttributeReturnNullOnNull() {
    assertNull(cut.convertToEntityAttribute(null));
  }

  @Test
  void checkConvertToDatabaseColumnReturnConvertedOnNonNull() {
    final ZonedDateTime time = ZonedDateTime
        .of(LocalDateTime.of(2020, 10, 20, 6, 23, 10), ZoneId.of(ZoneId.SHORT_IDS.get("ECT")));
    final OffsetDateTime act = cut.convertToDatabaseColumn(time);
    assertEquals("2020-10-20T06:23:10+02:00", act.toString());
  }

  @Test
  void checkConvertToEntityAttributeReturnConvertedOnNonNull() {
    final OffsetDateTime time = OffsetDateTime.of(
        LocalDateTime.of(2020, 10, 20, 6, 23), ZoneOffset.ofHours(3));
    final ZonedDateTime act = cut.convertToEntityAttribute(time);
    assertEquals(time, act.toOffsetDateTime());
  }
}
