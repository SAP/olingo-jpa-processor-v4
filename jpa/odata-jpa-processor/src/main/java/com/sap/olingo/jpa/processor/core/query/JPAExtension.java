package com.sap.olingo.jpa.processor.core.query;

import com.sap.olingo.jpa.processor.core.converter.JPAExpandResult;

public interface JPAExtension {
  /**
   * Process a expand query, which contains a $skip and/or a $top option.<p>
   * This a tricky problem, as it can not be done easily with SQL. It could be that a database offers special solutions.
   * There is an worth reading blog regards this topic:
   * <a href="http://www.xaprb.com/blog/2006/12/07/how-to-select-the-firstleastmax-row-per-group-in-sql/">How to select
   * the first/least/max row per group in SQL</a>
   * @return query result
   */
  JPAExpandResult executeExpandTopSkipQuery();
}
