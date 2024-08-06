package com.sap.olingo.jpa.processor.cb;

import java.util.Arrays;
import java.util.Collections;

import javax.annotation.Nonnull;

/**
 *
 *
 * @author Oliver Grande
 * 2024-07-03
 * 2.2.0
 */
public interface ProcessorSqlPatternProvider {
  static final String VALUE_PLACEHOLDER = "#VALUE#";
  static final String START_PLACEHOLDER = "#START#";
  static final String LENGTH_PLACEHOLDER = "#LENGTH#";
  static final String SEARCH_STRING_PLACEHOLDER = "#SEARCH_STRING#";
  static final String COMMA_SEPARATOR = ", ";
  // Pattern
  static final String LIMIT_PATTERN = "LIMIT " + VALUE_PLACEHOLDER;
  static final String OFFSET_PATTERN = "OFFSET " + VALUE_PLACEHOLDER;

  /**
   * Default pattern: LIMIT #VALUE#
   * @return Pattern for the clause limiting the number of rows returned
   */
  @Nonnull
  default String getMaxResultsPattern() {
    return LIMIT_PATTERN;
  }

  /**
   * Default pattern: OFFSET #VALUE#
   * @return Pattern for the clause defining the first row to be returned
   */
  @Nonnull
  default String getFirstResultPattern() {
    return OFFSET_PATTERN;
  }

  /**
   * Default: true
   * @return true if the database requires that the max result clause need to be before the first result clause
   */
  default boolean maxResultsFirst() {
    return true;
  }

  /**
   * Default pattern: SUBSTRING(#VALUE#, #START#, #LENGTH#)
   * <p>
   * The sub string of <i>value</i> from <i>start</i> with the length <i>length</i>.
   * <p>
   * @return Pattern for the sub string function
   */
  @Nonnull
  default ProcessorSqlFunction getSubStringPattern() {
    return new ProcessorSqlFunction("SUBSTRING", Arrays.asList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(COMMA_SEPARATOR, START_PLACEHOLDER, false),
        new ProcessorSqlParameter(COMMA_SEPARATOR, LENGTH_PLACEHOLDER, true)));
  }

  /**
   * Default pattern: CONCAT(#VALUE#, #VALUE#)
   * <p>
   * Concatenates two string. Some database, e.g. Postgresql, not having a named function to concatenate two strings,
   * but use an operator,
   * e.g. ||.
   * @return Pattern for a function that concatenates two strings
   */
  @Nonnull
  default ProcessorSqlPattern getConcatenatePattern() {
    return new ProcessorSqlFunction("CONCAT", Arrays.asList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(COMMA_SEPARATOR, VALUE_PLACEHOLDER, false)));
  }

  /**
   * Default pattern: LOCATE(#SEARCH_STRING#, #VALUE#, #START#)
   * <p>
   * Returns the position of the first occurrence of <i>search_string</i> in <i>value</i>. The search shall start at
   * <i>start</i>
   * <p>
   * The start position is seen as optional. In case the database supports an optional occurrences, to express that e.g.
   * the 3rd occurrence shall be found, it must not be mentioned here, as this is not supported by JPA.
   * @return Pattern for a function that searches for the position of a search string in another string/value.
   */
  @Nonnull
  default ProcessorSqlFunction getLocatePattern() {
    return new ProcessorSqlFunction("LOCATE", Arrays.asList(
        new ProcessorSqlParameter(SEARCH_STRING_PLACEHOLDER, false),
        new ProcessorSqlParameter(COMMA_SEPARATOR, VALUE_PLACEHOLDER, false),
        new ProcessorSqlParameter(COMMA_SEPARATOR, START_PLACEHOLDER, true)));
  }

  /**
   * Default pattern: LENGTH(#VALUE#)
   * <p>
   * The character length of <i>value</i>.
   * <p>
   * @return Pattern to determine the character length of a string.
   */
  @Nonnull
  default ProcessorSqlFunction getLengthPattern() {
    return new ProcessorSqlFunction("LENGTH", Collections.singletonList(
        new ProcessorSqlParameter(VALUE_PLACEHOLDER, false)));
  }
}
