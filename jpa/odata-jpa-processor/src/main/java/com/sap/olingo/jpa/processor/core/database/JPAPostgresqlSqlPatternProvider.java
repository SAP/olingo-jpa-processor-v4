package com.sap.olingo.jpa.processor.core.database;

import java.util.Arrays;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlFunction;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlParameter;
import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

public class JPAPostgresqlSqlPatternProvider implements ProcessorSqlPatternProvider {
  @Override
  public ProcessorSqlFunction getSubStringPattern() {
    // substring ( string text [ FROM start integer ] [ FOR count integer ] )
    return new ProcessorSqlFunction("SUBSTRING", Arrays.asList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(" FROM ", START_PLACEHOLDER, false),
        new ProcessorSqlParameter(" FOR ", LENGTH_PLACEHOLDER, true)));
  }

  @Override
  public ProcessorSqlFunction getLocatePattern() {
    // position ( substring text IN string text )
    return new ProcessorSqlFunction("POSITION", Arrays.asList(
        new ProcessorSqlParameter(SEARCH_STRING_PLACEHOLDER, false),
        new ProcessorSqlParameter(" IN ", VALUE_PLACEHOLDER, false)));
  }

}
