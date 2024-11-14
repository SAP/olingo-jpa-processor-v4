package com.sap.olingo.jpa.processor.core.database;

public class JPA_DERBY_DatabaseProcessor extends JPAAbstractDatabaseProcessor { // NOSONAR

  /**
   * See: <a href="https://db.apache.org/derby/docs/10.15/ref/rrefsqljtfinvoke.html">Derby: Function Invocation</a>
   */
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";

  public JPA_DERBY_DatabaseProcessor() {
    super();
  }

  @Override
  protected String functionSelectPattern() {
    return SELECT_BASE_PATTERN;
  }

  @Override
  protected String functionCountPattern() {
    return SELECT_COUNT_PATTERN;
  }
}
