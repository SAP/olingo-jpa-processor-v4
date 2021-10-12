package com.sap.olingo.jpa.metadata.reuse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * Created: 09.03.2020
 *
 */
public class TestOffsetDateTimeConverter {
  private OffsetDateTimeConverter cut;

  @BeforeEach
  public void setup() {
    cut = new OffsetDateTimeConverter();
  }

  @Test
  public void checkConvertToDatabaseColumnReturnNullOnNull() {
    assertNull(cut.convertToDatabaseColumn(null));
  }

  @Test
  public void checkConvertToEntityAttributeReturnNullOnNull() {
    assertNull(cut.convertToEntityAttribute(null));
  }

  @Test
  public void checkConvertToDatabaseColumnReturnConvertedOnNonNull() {
    final ZonedDateTime time = ZonedDateTime
        .of(LocalDateTime.of(2020, 10, 20, 6, 23, 10), ZoneId.of(ZoneId.SHORT_IDS.get("ECT")));
    final OffsetDateTime act = cut.convertToDatabaseColumn(time);
    assertEquals("2020-10-20T06:23:10+02:00", act.toString());
  }

  @Test
  public void checkConvertToEntityAttributeReturnConvertedOnNonNull() {
    final OffsetDateTime time = OffsetDateTime.of(
        LocalDateTime.of(2020, 10, 20, 6, 23), ZoneOffset.ofHours(3));
    final ZonedDateTime act = cut.convertToEntityAttribute(time);
    assertEquals(time, act.toOffsetDateTime());
  }
}
