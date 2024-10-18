package com.sap.olingo.jpa.processor.core.database;

import java.util.Arrays;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlFunction;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlOperator;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlParameter;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPattern;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

public class JPADerbySqlPatternProvider implements ProcessorSqlPatternProvider {
  static final String MAX_RESULTS_PATTERN = "FETCH NEXT " + VALUE_PLACEHOLDER + " ROWS ONLY";
  static final String FIRST_RESULT_PATTERN = "OFFSET " + VALUE_PLACEHOLDER + " ROWS";

  /**
   * <a href="https://db.apache.org/derby/docs/10.17/ref/rrefsqlj40899.html">Concatenation operator</a>
   */
  @Override
  public ProcessorSqlPattern getConcatenatePattern() {
    return new ProcessorSqlOperator(Arrays.asList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(" || ", VALUE_PLACEHOLDER, false)));
  }

  /**
   * <a href="https://db.apache.org/derby/docs/10.17/ref/rrefsqlj93082.html">SUBSTR function</a>
   */
  @Override
  public ProcessorSqlFunction getSubStringPattern() {
    return new ProcessorSqlFunction("SUBSTR", Arrays.asList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(START_PLACEHOLDER, false),
        new ProcessorSqlParameter(LENGTH_PLACEHOLDER, true)));
  }

  /**
   * <a href="https://db.apache.org/derby/docs/10.17/ref/rrefsqljoffsetfetch.html">Offset and fetch</a>
   */
  @Override
  public String getMaxResultsPattern() {
    return MAX_RESULTS_PATTERN;
  }

  /**
   * <a href="https://db.apache.org/derby/docs/10.17/ref/rrefsqljoffsetfetch.html">Offset and fetch</a>
   */
  @Override
  public String getFirstResultPattern() {
    return FIRST_RESULT_PATTERN;
  }

  @Override
  public boolean maxResultsFirst() {
    return false;
  }
}
