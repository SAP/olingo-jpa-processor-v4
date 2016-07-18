package org.apache.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAElement;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import org.apache.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;
import org.apache.olingo.server.api.uri.queryoption.search.SearchTerm;

final class JPA_HANA_DatabaseProcessor implements JPAODataDatabaseProcessor {
  private static final String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static final String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private static final String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  @Override
  public List<?> executeFunctionQuery(final UriResourceFunction uriResourceFunction, final JPAFunction jpaFunction,
      final JPAEntityType returnType, final EntityManager em) throws ODataApplicationException {

    final String queryString = generateQueryString(jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString, returnType.getTypeClass());
    int count = 1;
    for (final JPAFunctionParameter parameter : jpaFunction.getParameter()) {
      final UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
      final Object value = getValue(uriResourceFunction.getFunction(), parameter, uriParameter.getText());
      functionQuery.setParameter(count, value);
      count += 1;
    }
    return functionQuery.getResultList();
  }

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final Root<?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {
    /*
     * The following code generates a sub-query to filter on the values that matches the search term. This looks
     * cumbersome, but there were problems using the straight forward solution:
     * return cb.function("CONTAINS", Boolean.class, root.get("name"), cb.literal(term.getSearchTerm()));
     * in case $search was combined with $filter. In this case the processing aborts with the following error:
     * "org.eclipse.persistence.internal.jpa.querydef.FunctionExpressionImpl cannot be cast to
     * org.eclipse.persistence.internal.jpa.querydef.CompoundExpressionImpl"
     */
    List<JPAPath> searchableAttributes = null;
    JPAPath keyPath = null;
    try {
      searchableAttributes = entityType.getSearchablePath();
      final List<JPAPath> keyPathList = entityType.getKeyPath();
      if (keyPathList.size() == 1)
        keyPath = keyPathList.get(0);
      else
        throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.WRONG_NO_KEY_PROP,
            HttpStatusCode.INTERNAL_SERVER_ERROR);
    } catch (ODataJPAModelException e) {
      throw new ODataJPADBAdaptorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    if (!searchableAttributes.isEmpty()) {
      final SearchTerm term = searchOption.getSearchExpression().asSearchTerm();
      @SuppressWarnings("unchecked")
      final Subquery<Object> sq = (Subquery<Object>) cq.subquery(entityType.getKeyType());
      final Root<?> sr = sq.from(root.getJavaType());
      final Expression<Object> sel = sr.get(keyPath.getPath().get(0).getInternalName());
      sq.select(sel);
      Path<?> attributePath = sr;
      for (final JPAPath searchableAttribute : searchableAttributes) {
        for (final JPAElement pathItem : searchableAttribute.getPath())
          attributePath = attributePath.get(pathItem.getInternalName());
      }
      sq.where(cb.function("CONTAINS", Boolean.class, attributePath, cb.literal(term.getSearchTerm())));
      return cb.in(root.get(keyPath.getPath().get(0).getInternalName())).value(sq);
    }
    return null;
  }

  private String generateQueryString(final JPAFunction jpaFunction) {
    final StringBuffer parameterList = new StringBuffer();
    String queryString = SELECT_BASE_PATTERN;

    queryString = queryString.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
    for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
      parameterList.append(',');
      parameterList.append('?');
      parameterList.append(i);
    }
    parameterList.deleteCharAt(0);
    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
  }

  private UriParameter findParameterByExternalName(final JPAFunctionParameter parameter,
      final List<UriParameter> uriParameters)
      throws ODataApplicationException {
    for (final UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_MISSING,
        HttpStatusCode.BAD_REQUEST, parameter.getName());
  }

  private Object getValue(final EdmFunction edmFunction, final JPAFunctionParameter parameter, final String uriValue)
      throws ODataApplicationException {
    final String value = uriValue.replaceAll("'", "");
    final EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, parameter.maxLength(),
          parameter.precision(), parameter.scale(), true, parameter.getType());
    } catch (EdmPrimitiveTypeException e) {
      // Unable to convert value %1$s of parameter %2$s
      throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
          HttpStatusCode.NOT_IMPLEMENTED, uriValue, parameter.getName());
    }
  }

}
