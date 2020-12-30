package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SelectionImplTest {

  private SqlSelection<Long> selection;
  private SelectionImpl<Long> cut;

  @SuppressWarnings("unchecked")
  @BeforeEach
  void setup() {
    selection = mock(SqlSelection.class);
    when(selection.asSQL(any())).thenReturn(new StringBuilder("Dummy"));
    cut = new SelectionImpl<>(selection, Long.class, new AliasBuilder("X"));
  }

  @Test
  void testGetAliasReturnsGeneratedStringNotSet() {
    assertEquals("X0", cut.getAlias());
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

  @Test
  void testAsSql() {
    assertEquals("Dummy X0", cut.asSQL(new StringBuilder()).toString());
  }
}
