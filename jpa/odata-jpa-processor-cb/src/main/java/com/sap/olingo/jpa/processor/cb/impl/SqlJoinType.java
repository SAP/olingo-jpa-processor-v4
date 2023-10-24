package com.sap.olingo.jpa.processor.cb.impl;

import java.util.EnumMap;

import jakarta.persistence.criteria.JoinType;

enum SqlJoinType {

  INNER("INNER JOIN", JoinType.INNER),
  LEFT("LEFT OUTER JOIN", JoinType.LEFT),
  RIGHT("RIGHT OUTER JOIN", JoinType.RIGHT);

  private static final EnumMap<JoinType, SqlJoinType> REL = new EnumMap<>(JoinType.class);

  static SqlJoinType byJoinType(final JoinType jt) {
    final SqlJoinType s = REL.get(jt);
    if (s != null)
      return s;
    for (final SqlJoinType sql : SqlJoinType.values()) {
      if (sql.getJoinType() == jt) {
        REL.put(jt, sql);
        return sql;
      }
    }
    return null;
  }

  private final String keyWord;
  private final JoinType jt;

  private SqlJoinType(final String keyWord, final JoinType jt) {
    this.keyWord = keyWord;
    this.jt = jt;
  }

  JoinType getJoinType() {
    return jt;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
