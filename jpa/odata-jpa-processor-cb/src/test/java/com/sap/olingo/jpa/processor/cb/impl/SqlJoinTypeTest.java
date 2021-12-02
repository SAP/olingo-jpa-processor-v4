package com.sap.olingo.jpa.processor.cb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.criteria.JoinType;

import org.junit.jupiter.api.Test;

class SqlJoinTypeTest {

  @Test
  void testGetJoinTypeReturnsValue() {
    assertEquals(JoinType.INNER, SqlJoinType.INNER.getJoinType());
    assertEquals(JoinType.LEFT, SqlJoinType.LEFT.getJoinType());
    assertEquals(JoinType.RIGHT, SqlJoinType.RIGHT.getJoinType());
  }

  @Test
  void testByJoinType() {
    assertEquals(SqlJoinType.INNER, SqlJoinType.byJoinType(JoinType.INNER));
    assertEquals(SqlJoinType.INNER, SqlJoinType.byJoinType(JoinType.INNER));
  }
}
