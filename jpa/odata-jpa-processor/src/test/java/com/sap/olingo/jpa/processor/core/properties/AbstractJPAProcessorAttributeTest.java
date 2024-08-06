package com.sap.olingo.jpa.processor.core.properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

abstract class AbstractJPAProcessorAttributeTest {
  protected JPAPath path;
  protected JPAAssociationPath hop;
  protected JPAProcessorAttribute cut;

  @BeforeEach
  void setup() {
    path = mock(JPAPath.class);
    hop = mock(JPAAssociationPath.class);

    when(hop.getAlias()).thenReturn("hophop");
    when(path.getAlias()).thenReturn("path");
  }

  @Test
  void testReturnsDescending() {
    createCutSortOrder(true);
    assertTrue(cut.sortDescending());
  }

  @Test
  void testReturnsAscending() {
    createCutSortOrder(false);
    assertFalse(cut.sortDescending());
  }

  @Test
  void testGetPathThrowsExceptionIfSetTargetNotCalled() {

    createCutSortOrder(true);
    assertThrows(IllegalAccessError.class, () -> cut.getPath());
  }

  protected abstract void createCutSortOrder(boolean descending);

  abstract void testJoinRequired();

  abstract void testJoinNotRequired();

  abstract void testCreateJoinThrowsExceptionIfSetTargetNotCalled();
}
