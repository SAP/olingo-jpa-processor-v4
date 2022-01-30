package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;

class TestJPAOnConditionItemImpl {
  private JPAOnConditionItemImpl cut;

  @Test
  void checkThrowsExceptionOnMissingLeft() {
    final JPAPath rightAttribute = mock(JPAPath.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAOnConditionItemImpl(null, rightAttribute));
    assertEquals(MessageKeys.ON_LEFT_ATTRIBUTE_NULL.getKey(), act.getId());
  }

  @Test
  void checkThrowsExceptionOnMissingRight() {
    final JPAPath leftAttribute = mock(JPAPath.class);

    final ODataJPAModelException act = assertThrows(ODataJPAModelException.class,
        () -> new JPAOnConditionItemImpl(leftAttribute, null));
    assertEquals(MessageKeys.ON_RIGHT_ATTRIBUTE_NULL.getKey(), act.getId());
  }

  @Test
  void checkReturnProvidedValues() throws ODataJPAModelException {
    final JPAPath leftAttribute = mock(JPAPath.class);
    final JPAPath rightAttribute = mock(JPAPath.class);
    cut = new JPAOnConditionItemImpl(leftAttribute, rightAttribute);
    assertEquals(leftAttribute, cut.getLeftPath());
    assertEquals(rightAttribute, cut.getRightPath());
  }
}
