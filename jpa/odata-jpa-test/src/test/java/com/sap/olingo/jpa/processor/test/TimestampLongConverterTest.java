package com.sap.olingo.jpa.processor.test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;

import com.sap.olingo.jpa.processor.core.testmodel.TimestampLongConverter;

final class TimestampLongConverterTest extends AbstractConverterTest<Timestamp, Long> {

  @BeforeEach
  void setup() {
    cut = new TimestampLongConverter();
    exp = Timestamp.valueOf(LocalDateTime.of(2020, Month.FEBRUARY, 29, 12, 0, 0));
  }
}
