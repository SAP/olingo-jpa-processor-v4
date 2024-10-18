package com.sap.olingo.jpa.processor.core.uri;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPASkipOptionImplTest {
  private JPASkipTokenOptionImpl cut;

  @BeforeEach
  void setup() {
    cut = new JPASkipTokenOptionImpl("Token");
  }

  @Test
  void testGetName() {
    assertNull(cut.getName());
  }

  @Test
  void testGetText() {
    assertEquals("Token", cut.getText());
  }

  @Test
  void testGetValue() {
    assertEquals("Token", cut.getValue());
  }

  @Test
  void testGetKind() {
    assertEquals(SystemQueryOptionKind.SKIPTOKEN, cut.getKind());
  }

}
