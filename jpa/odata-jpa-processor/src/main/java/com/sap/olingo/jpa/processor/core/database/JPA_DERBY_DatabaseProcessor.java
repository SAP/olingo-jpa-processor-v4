package com.sap.olingo.jpa.processor.core.database;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.queryoption.SearchOption;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import com.sap.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import com.sap.olingo.jpa.processor.core.exception.ODataJPADBAdaptorException;

class JPA_DERBY_DatabaseProcessor implements JPAODataDatabaseProcessor {
  private static final String SELECT_BASE_PATTERN = "SELECT f.* FROM TABLE ($FUNCTIONNAME$($PARAMETER$)) f";
  private static final String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private static final String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  @Override
  public Expression<Boolean> createSearchWhereClause(final CriteriaBuilder cb, final CriteriaQuery<?> cq,
      final From<?, ?> root, final JPAEntityType entityType, final SearchOption searchOption)
      throws ODataApplicationException {
    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.NOT_SUPPORTED_SEARCH,
        HttpStatusCode.NOT_IMPLEMENTED);
  }

  @Override
  public <T> java.util.List<T> executeFunctionQuery(final List<UriResource> uriResourceParts,
      final JPADataBaseFunction jpaFunction, final EntityManager em)
      throws ODataApplicationException {

    final String queryString = generateQueryString(jpaFunction);
    final Query functionQuery = em.createNativeQuery(queryString, jpaFunction.getResultParameter().getType());
//    int count = 1;
//    for (final JPAFunctionParameter parameter : jpaFunction.getParameter()) {
//      final UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
//      final Object value = getValue(uriResourceFunction.getFunction(), parameter, uriParameter.getText());
//      functionQuery.setParameter(count, value);
//      count += 1;
//    }
    return functionQuery.getResultList();
  }

  private String generateQueryString(final JPAFunction jpaFunction) {
//    final StringBuffer parameterList = new StringBuffer();
//    String queryString = SELECT_BASE_PATTERN;
//
//    queryString = queryString.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
//    for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
//      parameterList.append(',');
//      parameterList.append('?');
//      parameterList.append(i);
//    }
//    parameterList.deleteCharAt(0);
//    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
    return null;
  }
//
//  private UriParameter findParameterByExternalName(final JPAFunctionParameter parameter,
//      final List<UriParameter> uriParameters)
//      throws ODataApplicationException {
//    for (final UriParameter uriParameter : uriParameters) {
//      if (uriParameter.getName().equals(parameter.getName()))
//        return uriParameter;
//    }
//    throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_MISSING,
//        HttpStatusCode.BAD_REQUEST, parameter.getName());
//  }
//
//  private Object getValue(final EdmFunction edmFunction, final JPAFunctionParameter parameter, final String uriValue)
//      throws ODataApplicationException {
//    final String value = uriValue.replaceAll("'", "");
//    final EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
//    try {
//      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, parameter.maxLength(),
//          parameter.precision(), parameter.scale(), true, parameter.getType());
//    } catch (EdmPrimitiveTypeException e) {
//      // Unable to convert value %1$s of parameter %2$s
//      throw new ODataJPADBAdaptorException(ODataJPADBAdaptorException.MessageKeys.PARAMETER_CONVERSION_ERROR,
//          HttpStatusCode.NOT_IMPLEMENTED, uriValue, parameter.getName());
//    }
//  }
}
