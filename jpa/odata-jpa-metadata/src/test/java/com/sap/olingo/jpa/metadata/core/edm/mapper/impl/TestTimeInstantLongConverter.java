/**
 * 
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.converter.TimeInstantLongConverter;

/**
 * 
 * @author D023143
 * @since 1.0.0-RC
 */
public class TestTimeInstantLongConverter {
  private static final String FIVE_DAYS_LATER = "1970-01-06T00:00:00Z";
  private static final long FIVE_DAYS = 5 * 24 * 60 * 60 * 1000;
  private TimeInstantLongConverter cut;

  @BeforeEach
  public void setup() {
    cut = new TimeInstantLongConverter();
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
    final Instant time = Instant.parse(FIVE_DAYS_LATER);
    final Long act = cut.convertToDatabaseColumn(time);
    assertEquals(FIVE_DAYS, act);
  }

  @Test
  public void checkConvertToEntityAttributeReturnConvertedOnNonNull() {
    final Instant act = cut.convertToEntityAttribute(FIVE_DAYS);
    assertEquals(FIVE_DAYS_LATER, act.toString());
  }
}
