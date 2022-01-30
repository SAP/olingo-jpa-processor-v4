package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AliasBuilderTest {
  private AliasBuilder cut;

  @BeforeEach
  void setup() {
    cut = new AliasBuilder();
  }

  @Test
  void getFirstAlias() {
    assertEquals("E0", cut.getNext());
  }

  @Test
  void getSecondAlias() {
    cut.getNext();
    assertEquals("E1", cut.getNext());
  }
}
