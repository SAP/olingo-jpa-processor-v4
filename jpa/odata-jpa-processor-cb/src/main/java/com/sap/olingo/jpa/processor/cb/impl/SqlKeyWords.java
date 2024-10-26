package com.sap.olingo.jpa.processor.cb.impl;

enum SqlKeyWords {
  ADD("ADD"),
  ALL("ALL"),
  AND("AND"),
  ANY("ANY"),
  ASC("ASC"),
  BETWEEN("BETWEEN"),
  COALESCE("COALESCE"),
  CONTAINS("CONTAINS"),
  DESC("DESC"),
  DISTINCT("DISTINCT"),
  ESCAPE("ESCAPE"),
  EXISTS("EXISTS"),
  FROM("FROM"),
  GROUPBY("GROUP BY"),
  HAVING("HAVING"),
  IN("IN"),
  LIKE("LIKE"),
  MOD("MOD"),
  NOT("NOT"),
  ORDERBY("ORDER BY"),
  OVER("OVER"),
  PARTITION("PARTITION BY"),
  SELECT("SELECT"),
  SET("SET"),
  SOME("SOME"),
  UNION("UNION"),
  UPDATE("UPDATE"),
  WHERE("WHERE");

  private final String keyWord;

  private SqlKeyWords(final String keyWord) {
    this.keyWord = keyWord;
  }

  @Override
  public String toString() {
    return keyWord;
  }
}
