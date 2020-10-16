package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TestSelectionPathInfo {
  private SelectionPathInfo<Integer> cut;

  @Test
  public void testSetTripleEmptySets() {
    cut = new SelectionPathInfo<>(new HashSet<>(), new HashSet<>(), new HashSet<>());
    assertNotNull(cut);
    assertNotNull(cut.getODataSelections());
    assertNotNull(cut.getRequitedSelections());
    assertNotNull(cut.getTransientSelections());
    assertNotNull(cut.joined());
  }

  @Test
  public void testSetTripleFirstNull() {
    cut = new SelectionPathInfo<>(null, new HashSet<>(), new HashSet<>());
    assertNotNull(cut);
    assertNotNull(cut.getODataSelections());
    assertNotNull(cut.getRequitedSelections());
    assertNotNull(cut.getTransientSelections());
    assertNotNull(cut.joined());
  }

  @Test
  public void testSetTripleSecondNull() {
    cut = new SelectionPathInfo<>(new HashSet<>(), null, new HashSet<>());
    assertNotNull(cut);
    assertNotNull(cut.getODataSelections());
    assertNotNull(cut.getRequitedSelections());
    assertNotNull(cut.getTransientSelections());
    assertNotNull(cut.joined());
  }

  @Test
  public void testSetTripleThirdNull() {
    cut = new SelectionPathInfo<>(new HashSet<>(), new HashSet<>(), null);
    assertNotNull(cut);
    assertNotNull(cut.getODataSelections());
    assertNotNull(cut.getRequitedSelections());
    assertNotNull(cut.getTransientSelections());
    assertNotNull(cut.joined());
  }

  @Test
  public void testJoinedDoesNotReturnDuplicates() {
    final Set<Integer> first = new HashSet<>(Arrays.asList(1, 3, 5, 7, 9, 2));
    final Set<Integer> second = new HashSet<>(Arrays.asList(1, 4, 6, 2));
    final Set<Integer> third = new HashSet<>(Arrays.asList(7, 8));
    final Set<Integer> exp = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9));

    cut = new SelectionPathInfo<>(first, second, third);
    assertNotNull(cut);
    assertEquals(exp, cut.joined());
  }

  @Test
  public void testJoinedPersistantDoesNotReturnTransient() {
    final Set<Integer> first = new HashSet<>(Arrays.asList(1, 3, 5, 7, 9, 2));
    final Set<Integer> second = new HashSet<>(Arrays.asList(1, 4, 6, 2));
    final Set<Integer> third = new HashSet<>(Arrays.asList(7, 8));
    final Set<Integer> exp = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 9));

    cut = new SelectionPathInfo<>(first, second, third);
    assertNotNull(cut);
    assertEquals(exp, cut.joinedPersistent());
  }

  @Test
  public void testJoinedRequestedDoesNotReturnRequired() {
    final Set<Integer> first = new HashSet<>(Arrays.asList(1, 3, 5, 7, 9, 2));
    final Set<Integer> second = new HashSet<>(Arrays.asList(1, 4, 6, 2));
    final Set<Integer> third = new HashSet<>(Arrays.asList(7, 8));
    final Set<Integer> exp = new HashSet<>(Arrays.asList(1, 2, 3, 5, 7, 8, 9));

    cut = new SelectionPathInfo<>(first, second, third);
    assertNotNull(cut);
    assertEquals(exp, cut.joinedRequested());
  }
}
