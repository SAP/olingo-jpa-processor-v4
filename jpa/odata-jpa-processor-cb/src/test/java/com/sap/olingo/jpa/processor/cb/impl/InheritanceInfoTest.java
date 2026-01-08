package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import jakarta.persistence.InheritanceType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.cb.testobjects.SubJoined;
import com.sap.olingo.jpa.processor.cb.testobjects.SubTablePerClass;
import com.sap.olingo.jpa.processor.core.testmodel.Organization;

class InheritanceInfoTest {
  private InheritanceInfo cut;
  private JPAEntityType et;

  @BeforeEach
  void setup() {
    et = mock(JPAEntityType.class);
  }

  @Test
  void testNoInheritance() {
    doReturn(Long.class).when(et).getTypeClass();
    cut = new InheritanceInfo(et);
    assertFalse(cut.getBaseClass().isPresent());
  }

  @Test
  void testDiscriminatorColumn() {
    doReturn(Organization.class).when(et).getTypeClass();
    cut = new InheritanceInfo(et);
    assertTrue(cut.getBaseClass().isPresent());
    assertEquals(InheritanceType.SINGLE_TABLE, cut.getInheritanceType().get());
    assertEquals("\"Type\"", cut.getDiscriminatorColumn().get());
  }

  @Test
  void testJoined() {
    doReturn(SubJoined.class).when(et).getTypeClass();
    cut = new InheritanceInfo(et);
    assertTrue(cut.getBaseClass().isPresent());
    assertEquals(InheritanceType.JOINED, cut.getInheritanceType().get());
    assertFalse(cut.getDiscriminatorColumn().isPresent());
  }

  @Test
  void testTablePerClass() {
    doReturn(SubTablePerClass.class).when(et).getTypeClass();
    cut = new InheritanceInfo(et);
    assertTrue(cut.getBaseClass().isPresent());
    assertEquals(InheritanceType.TABLE_PER_CLASS, cut.getInheritanceType().get());
    assertFalse(cut.getDiscriminatorColumn().isPresent());
  }
}
