package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AppliesTo;

class AppliesToTest {

  @TestFactory
  Stream<DynamicTest> checkEnumHasValue() {
    return Stream.of(AppliesTo.values())
        .map(a -> dynamicTest(a.name(), () -> {
          assertNotNull(a.value());
          assertFalse(a.value().isEmpty());
        }));
  }
}
