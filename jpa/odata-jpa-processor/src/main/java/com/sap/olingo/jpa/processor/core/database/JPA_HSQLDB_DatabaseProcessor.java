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

public class JPA_HSQLDB_DatabaseProcessor extends JPAAbstractDatabaseProcessor { // NOSONAR
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final From<?, ?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {

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
