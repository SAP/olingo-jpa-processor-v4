package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class JPAClaimsPairTest {

  @Test
  void checkCreateIntegerPairOnlyMin() {
    final JPAClaimsPair<Integer> cut = new JPAClaimsPair<>(7);
    assertEquals((Integer) 7, cut.min);
    assertEquals((Integer) null, cut.max);
  }

  @Test
  void checkCreateIntegerPairNoUpperBoundary() {
    final JPAClaimsPair<Integer> cut = new JPAClaimsPair<>(7);
    assertFalse(cut.hasUpperBoundary);
  }

  @Test
  void checkCreateIntegerPair() {
    final JPAClaimsPair<Integer> cut = new JPAClaimsPair<>(7, 10);
    assertEquals((Integer) 7, cut.min);
    assertEquals((Integer) 10, cut.max);
  }

  @Test
  void checkCreateIntegerPairUpperBoundary() {
    final JPAClaimsPair<Integer> cut = new JPAClaimsPair<>(7, 10);
    assertTrue(cut.hasUpperBoundary);
  }

  @Test
  void checkHasToStringMethod() {
    final JPAClaimsPair<Integer> cut = new JPAClaimsPair<>(7, 10);
    final String act = cut.toString();
    assertNotNull(act);
    assertTrue(act.contains("7"));
  }

  @Test
  void checkCastIntegerValueToLong() {
    final JPAClaimsPair<Integer> cut = new JPAClaimsPair<>(7, 10);
    assertEquals(7, (int) cut.minAs());
    assertEquals(10, (int) cut.maxAs());
  }
}
