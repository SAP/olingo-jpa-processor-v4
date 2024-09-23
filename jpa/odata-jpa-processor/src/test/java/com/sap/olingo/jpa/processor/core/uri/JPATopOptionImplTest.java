package com.sap.olingo.jpa.processor.core.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPATopOptionImplTest {
  private JPATopOptionImpl cut;

  @BeforeEach
  void setup() {
    cut = new JPATopOptionImpl(100);
  }

  @Test
  void testGetName() {
    assertNull(cut.getName());
  }

  @Test
  void testGetText() {
    assertEquals("100", cut.getText());
  }

  @Test
  void testGetValue() {
    assertEquals(100, cut.getValue());
  }

  @Test
  void testGetKind() {
    assertEquals(SystemQueryOptionKind.TOP, cut.getKind());
  }

}
