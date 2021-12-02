/**
 *
 */
package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Oliver Grande
 * Created: 14.02.2020
 *
 */
class TestJPAProtectionInfo {
  private JPAProtectionInfo cut;

  @BeforeEach
  void setup() {
    cut = new JPAProtectionInfo(Arrays.asList("AdministrativeInformation/Created/By"), true);
  }

  @Test
  void checkToStringContainsPath() {
    assertNotNull(cut.toString());
    assertTrue(cut.toString().contains("AdministrativeInformation/Created/By"));
  }

  @Test
  void checkWildcardsTrue() {
    assertTrue(cut.supportsWildcards());
  }

  @Test
  void checkPathGetsReturned() {
    final List<String> act = cut.getPath();
    assertNotNull(act);
    assertEquals(1, act.size());
    assertEquals("AdministrativeInformation/Created/By", act.get(0));
  }

  @Test
  void checkWildcardsReturnsFalseForInteger() {
    assertFalse(cut.supportsWildcards(Integer.class));
  }

  @Test
  void checkWildcardsReturnsFalseForDate() {
    assertFalse(cut.supportsWildcards(LocalDate.class));
  }

  @Test
  void checkWildcardsReturnsTrueForString() {
    assertTrue(cut.supportsWildcards(String.class));
  }
}
