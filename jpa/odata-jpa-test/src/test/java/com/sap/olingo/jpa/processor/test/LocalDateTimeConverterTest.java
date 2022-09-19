package com.sap.olingo.jpa.processor.test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;

import com.sap.olingo.jpa.processor.core.testmodel.LocalDateTimeConverter;

final class LocalDateTimeConverterTest extends AbstractConverterTest<LocalDateTime, Date> {

  @BeforeEach
  void setup() {
    cut = new LocalDateTimeConverter();
    exp = LocalDateTime.of(2020, Month.FEBRUARY, 29, 12, 0, 0);
  }
}
