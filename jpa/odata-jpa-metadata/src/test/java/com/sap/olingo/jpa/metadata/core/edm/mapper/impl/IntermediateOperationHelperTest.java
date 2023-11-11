package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.testobjects.TestActionCollection;

class IntermediateOperationHelperTest {

  static Stream<Arguments> provideIsCollection() {
    return Stream.of(
        Arguments.of(ArrayList.class, true),
        Arguments.of(List.class, true),
        Arguments.of(TestActionCollection.class, true),
        Arguments.of(Integer.class, false),
        Arguments.of(HashMap.class, false),
        Arguments.of(IntermediateProperty.class, false));
  }

  @ParameterizedTest
  @MethodSource("provideIsCollection")
  void testIsCollection(final Class<?> clazz, final boolean expected) {
    assertEquals(expected, IntermediateOperationHelper.isCollection(clazz));
  }
}
