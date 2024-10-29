package com.sap.olingo.jpa.processor.core.database;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;

/**
 * Sample implementation a database processor for PostgreSQL
 *
 * @author Oliver Grande
 * Created: 04.07.2019
 *
 * @deprecated Use JPAPostgresqlDatabaseProcessor instead
 */
@Deprecated(since = "2.2.0", forRemoval = true)
public class JPA_POSTSQL_DatabaseProcessor extends JPAAbstractDatabaseProcessor { // NOSONAR
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM $FUNCTIONNAME$($PARAMETER$)";

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final From<?, ?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {

    /*
     * Even so PostgesSQL has text search, as of know no generic implementation made for search
     */
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.NOT_SUPPORTED_SEARCH,
        HttpStatusCode.NOT_IMPLEMENTED);
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
