package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.HashMap;
import java.util.stream.Stream;

import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

class TestIntermediateModelElement {
  private IntermediateModelElementTest cut;

  static Stream<Arguments> boxPrimitive() {
    return Stream.of(
        arguments(int.class, Integer.class),
        arguments(Integer.class, Integer.class),
        arguments(long.class, Long.class),
        arguments(Long.class, Long.class),
        arguments(short.class, Short.class),
        arguments(Short.class, Short.class),
        arguments(float.class, Float.class),
        arguments(Float.class, Float.class),
        arguments(double.class, Double.class),
        arguments(Double.class, Double.class),
        arguments(byte.class, Byte.class),
        arguments(Byte.class, Byte.class),
        arguments(boolean.class, Boolean.class),
        arguments(Boolean.class, Boolean.class),
        arguments(char.class, Character.class),
        arguments(Character.class, Character.class));
  }

  @BeforeEach
  void setup() {
    cut = new IntermediateModelElementTest(new JPADefaultEdmNameBuilder("Dummy"), "Test");
  }

  @ParameterizedTest
  @MethodSource("boxPrimitive")
  void checkBoxPrimitive(final Class<?> type, final Class<?> result) {
    assertEquals(result, cut.boxPrimitive(type));
  }

  @Test
  void checkToString() {
    assertTrue(cut.toString().contains("Test"));
  }

  @Test
  void checkFindModelElementByEdmItemReturnsNull() {
    assertNull(cut.findModelElementByEdmItem("Willi", new HashMap<>()));
  }

  private class IntermediateModelElementTest extends IntermediateModelElement {

    public IntermediateModelElementTest(final JPADefaultEdmNameBuilder nameBuilder, final String internalName) {
      super(nameBuilder, internalName);
    }

    @Override
    protected void lazyBuildEdmItem() throws ODataJPAModelException {
      // DO nothing

    }

    @Override
    CsdlAbstractEdmItem getEdmItem() throws ODataJPAModelException {
      return null;
    }

  }
}
