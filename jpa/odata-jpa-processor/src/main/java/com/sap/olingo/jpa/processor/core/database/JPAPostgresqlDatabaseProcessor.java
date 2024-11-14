package com.sap.olingo.jpa.processor.core.database;

/**
 * Sample implementation a database processor for PostgreSQL
 *
 * @author Oliver Grande
 * Created: 04.07.2019
 *
 */
public class JPAPostgresqlDatabaseProcessor extends JPAAbstractDatabaseProcessor { // NOSONAR
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM $FUNCTIONNAME$($PARAMETER$)";

  public JPAPostgresqlDatabaseProcessor() {
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
