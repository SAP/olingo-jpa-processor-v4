package com.sap.olingo.jpa.processor.test;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;

import com.sap.olingo.jpa.processor.core.testmodel.UUIDToBinaryConverter;

final class UUIDToBinaryConverterTest extends AbstractConverterTest<UUID, byte[]> {

  @BeforeEach
  void setup() {
    cut = new UUIDToBinaryConverter();
    exp = UUID.randomUUID();
  }
}
