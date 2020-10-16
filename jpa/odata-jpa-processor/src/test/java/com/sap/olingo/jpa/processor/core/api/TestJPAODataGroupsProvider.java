package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestJPAODataGroupsProvider {
  private JPAODataGroupsProvider cut;

  @BeforeEach
  public void setup() {
    cut = new JPAODataGroupsProvider();
  }

  @Test
  public void getEmptyListIfNoGroupProvided() {
    assertNotNull(cut.getGroups());
    assertTrue(cut.getGroups().isEmpty());
  }

  @Test
  public void getReturnsOneProvidedGroup() {
    cut.addGroup("Willi");
    assertEquals(1, cut.getGroups().size());
    assertEquals("Willi", cut.getGroups().get(0));
  }

  @Test
  public void getReturnsTwoSeparateProvidedGroup() {
    cut.addGroup("Willi");
    cut.addGroup("Hugo");
    assertEquals(2, cut.getGroups().size());
    assertTrue(cut.getGroups().contains("Willi"));
    assertTrue(cut.getGroups().contains("Hugo"));
  }

  @Test
  public void getReturnsOneIgnoreNullSeperateProvidedGroup() {
    cut.addGroup("Willi");
    cut.addGroup(null);
    assertEquals(1, cut.getGroups().size());
    assertTrue(cut.getGroups().contains("Willi"));
  }

  @Test
  public void getReturnsProvidedGroupArray() {
    cut.addGroups("Hugo", "Willi");
    assertEquals(2, cut.getGroups().size());
    assertTrue(cut.getGroups().contains("Willi"));
    assertTrue(cut.getGroups().contains("Hugo"));
  }

  @Test
  public void getReturnsProvidedGroupCollection() {
    cut.addGroups(Arrays.asList("Hugo", null, "Willi"));
    assertEquals(2, cut.getGroups().size());
    assertTrue(cut.getGroups().contains("Willi"));
    assertTrue(cut.getGroups().contains("Hugo"));
  }
}
