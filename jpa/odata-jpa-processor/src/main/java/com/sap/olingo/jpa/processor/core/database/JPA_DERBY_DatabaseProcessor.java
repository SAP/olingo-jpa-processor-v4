package com.sap.olingo.jpa.processor.core.database;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException.MessageKeys.NOT_SUPPORTED_FUNC_WITH_NAVI;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceKind;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

public class JPA_DERBY_DatabaseProcessor extends JPAAbstractDatabaseProcessor { // NOSONAR
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";
  private static final String SELECT_COUNT_PATTERN = "SELECT COUNT(*) FROM TABLE ($FUNCTIONNAME$($PARAMETER$))";

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final From<?, ?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.NOT_SUPPORTED_SEARCH,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  /**
   * See: <a href="https://db.apache.org/derby/docs/10.15/ref/rrefsqljtfinvoke.html">Derby: Function Invocation</a>
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> java.util.List<T> executeFunctionQuery(final List<UriResource> uriResourceParts,
      final JPADataBaseFunction jpaFunction, final EntityManager em) throws ODataApplicationException {

    final UriResource last = uriResourceParts.get(uriResourceParts.size() - 1);

    if (last.getKind() == UriResourceKind.count) {
      final List<Long> countResult = new ArrayList<>();
      countResult.add(executeCountQuery(uriResourceParts, jpaFunction, em, SELECT_COUNT_PATTERN));
      return (List<T>) countResult;
    }
    if (last.getKind() == UriResourceKind.function)
      return executeQuery(uriResourceParts, jpaFunction, em, SELECT_BASE_PATTERN);
    throw new ODataJPAProcessorException(NOT_SUPPORTED_FUNC_WITH_NAVI, HttpStatusCode.NOT_IMPLEMENTED);

  }

}
