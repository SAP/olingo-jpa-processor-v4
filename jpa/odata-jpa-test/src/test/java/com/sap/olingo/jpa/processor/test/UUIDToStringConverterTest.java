package com.sap.olingo.jpa.processor.test;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import com.sap.olingo.jpa.processor.core.testmodel.UUIDToStringConverter;

final class UUIDToStringConverterTest extends AbstractConverterTest<UUID, String> {

  @BeforeEach
  void setup() {
    cut = new UUIDToStringConverter();
    exp = UUID.randomUUID();
  }
}