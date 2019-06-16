package com.sap.olingo.jpa.processor.core.util;

import org.apache.olingo.server.api.ODataApplicationException;
import org.mockito.ArgumentMatcher;

import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

public class CountQueryMatcher implements ArgumentMatcher<JPACountQuery> {

  private final long extCountResult;
  private boolean executed = false;

  public CountQueryMatcher(long exp) {
    extCountResult = exp;
  }

  @Override
  public boolean matches(JPACountQuery query) {
    if (query != null) {
      if (extCountResult != 0 && !executed) {
        try {
          executed = true; // Query can be used only once but matcher is called twice
          return extCountResult == query.countResults();
        } catch (ODataApplicationException e) {
          System.out.print(e.getMessage());
          return false;
        }
      }
      return true;
    }
    return false;
  }

}
