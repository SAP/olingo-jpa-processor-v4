package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Optional;

import javax.annotation.Nullable;

import com.sap.olingo.jpa.processor.cb.ProcessorSqlPatternProvider;

abstract class Pageable {

  final ProcessorSqlPatternProvider sqlPattern;
  private Optional<Integer> maxResults;
  private Optional<Integer> firstResult;

  Pageable(ProcessorSqlPatternProvider sqlPattern) {
    super();
    this.sqlPattern = sqlPattern;
    this.firstResult = Optional.empty();
    this.maxResults = Optional.empty();
  }

  /**
   * The position of the first result the query object was set to
   * retrieve. Returns 0 if <code>setFirstResult</code> was not applied to the
   * query object.
   * @return position of the first result
   */
  int getStartResult() {
    return firstResult.orElse(0);
  }

  void setStartResult(@Nullable final Integer startPosition) {
    firstResult = Optional.ofNullable(startPosition);
  }

  /**
   * The maximum number of results the query object was set to
   * retrieve. Returns <code>Integer.MAX_VALUE</code> if <code>setMaxResults</code> was not
   * applied to the query object.
   * @return maximum number of results
   * @since 2.0
   */
  int getMaxResults() {
    return maxResults.orElse(Integer.MAX_VALUE);
  }

  void setNumberOfResults(@Nullable Integer maxResult) {
    this.maxResults = Optional.ofNullable(maxResult);
  }

  void paging(final StringBuilder statement) {
    if (sqlPattern.maxResultsFirst()) {
      maxResults.ifPresent(limit -> statement.append(" ")
          .append(SqlPagingFunctions.LIMIT.toString(sqlPattern, limit)));
      firstResult.ifPresent(offset -> statement.append(" ")
          .append(SqlPagingFunctions.OFFSET.toString(sqlPattern, offset)));
    } else {
      firstResult.ifPresent(offset -> statement.append(" ")
          .append(SqlPagingFunctions.OFFSET.toString(sqlPattern, offset)));
      maxResults.ifPresent(limit -> statement.append(" ")
          .append(SqlPagingFunctions.LIMIT.toString(sqlPattern, limit)));
    }
  }
}
