package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;

class TestJPAOnConditionItemImpl {
  private JPAOnConditionItemImpl cut;

  @Test
  void checkThrowsExceptionOnMissingLeft() {
    final JPAPath rightAttribute = mock(JPAPath.class);

    final NullPointerException act = assertThrows(NullPointerException.class,
        () -> new JPAOnConditionItemImpl(null, rightAttribute));
    assertEquals("ON condition left attribute is null / not found.", act.getMessage());
  }

  @Test
  void checkThrowsExceptionOnMissingRight() {
    final JPAPath leftAttribute = mock(JPAPath.class);

    final NullPointerException act = assertThrows(NullPointerException.class,
        () -> new JPAOnConditionItemImpl(leftAttribute, null));
    assertEquals("ON condition right attribute is null / not found.", act.getMessage());
  }

  @Test
  void checkReturnProvidedValues() {
    final JPAPath leftAttribute = mock(JPAPath.class);
    final JPAPath rightAttribute = mock(JPAPath.class);
    cut = new JPAOnConditionItemImpl(leftAttribute, rightAttribute);
    assertEquals(leftAttribute, cut.getLeftPath());
    assertEquals(rightAttribute, cut.getRightPath());
  }
}
