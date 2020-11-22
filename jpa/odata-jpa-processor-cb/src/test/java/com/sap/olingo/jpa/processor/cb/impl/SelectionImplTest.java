package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import javax.persistence.criteria.Selection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SelectionImplTest {

  private List<Selection<?>> selections;
  private SelectionImpl<Long> cut;

  @BeforeEach
  void setup() {
    cut = new SelectionImpl<>(selections, Long.class);
  }

  @Test
  void testGetAliasReturnsEmptyStringNotSet() {
    assertEquals("", cut.getAlias());
  }

  @Test
  void testGetAliasReturnsSetValue() {
    assertEquals(cut, cut.alias("Selection"));
    assertEquals("Selection", cut.getAlias());
  }

  @Test
  void testAliasValueNotChanged() {
    assertEquals(cut, cut.alias("Selection"));
    assertEquals(cut, cut.alias("Error"));
    assertEquals("Selection", cut.getAlias());
  }

  @Test
  void testGetJavaType() {
    assertEquals(Long.class, cut.getJavaType());
  }

  @Test
  void testCompoundSelections() {
    assertFalse(cut.isCompoundSelection());
    assertThrows(IllegalStateException.class, () -> cut.getCompoundSelectionItems());
  }
}
