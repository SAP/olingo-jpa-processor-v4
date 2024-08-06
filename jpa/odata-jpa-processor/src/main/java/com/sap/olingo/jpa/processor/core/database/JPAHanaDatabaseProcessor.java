package com.sap.olingo.jpa.processor.core.database;

import java.util.Arrays;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlFunction;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlParameter;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

/**
 * Sample implementation a database processor for SAP HANA
 *
 * @author Oliver Grande
 * Created: 04.07.2024
 * @since
 */
public class JPAHanaDatabaseProcessor extends JPAAbstractDatabaseProcessor implements
    ProcessorSqlPatternProvider { // NOSONAR
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM $FUNCTIONNAME$($PARAMETER$)";

  public JPAHanaDatabaseProcessor() {
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

  @Override
  public ProcessorSqlFunction getLocatePattern() {
    // INSTR: Returns the position of the first occurrence of the second string within the first string (>= 1) or 0, if
    // the second string is not contained in the first.
    return new ProcessorSqlFunction("INSTR", Arrays.asList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(COMMA_SEPARATOR, SEARCH_STRING_PLACEHOLDER, false)));
  }

}
