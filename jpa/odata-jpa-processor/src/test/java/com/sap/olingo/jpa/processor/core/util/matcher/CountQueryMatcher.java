package com.sap.olingo.jpa.processor.core.util.matcher;

import org.apache.olingo.server.api.ODataApplicationException;
import org.mockito.ArgumentMatcher;

import com.sap.olingo.jpa.processor.core.query.JPACountQuery;

public class CountQueryMatcher implements ArgumentMatcher<JPACountQuery> {

  private final long expCountResult;
  private boolean executed = false;

  public CountQueryMatcher(final long exp) {
    expCountResult = exp;
  }

  @Override
  public boolean matches(final JPACountQuery query) {
    if (query != null) {
      if (expCountResult != 0 && !executed) {
        try {
          executed = true; // Query can be used only once but matcher is called twice
          return expCountResult == query.countResults();
        } catch (final ODataApplicationException e) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

}
