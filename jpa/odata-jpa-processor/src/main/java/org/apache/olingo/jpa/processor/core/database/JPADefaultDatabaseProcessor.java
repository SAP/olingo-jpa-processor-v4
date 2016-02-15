package org.apache.olingo.jpa.processor.core.database;

import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.olingo.commons.api.edm.EdmFunction;
import org.apache.olingo.commons.api.edm.EdmParameter;
import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import org.apache.olingo.jpa.processor.core.api.JPAODataDatabaseProcessor;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResourceFunction;

class JPADefaultDatabaseProcessor implements JPAODataDatabaseProcessor {
  private static String SELECT_BASE_PATTERN = "SELECT * FROM $FUNCTIONNAME$($PARAMETER$)";
  private static String FUNC_NAME_PLACEHOLDER = "$FUNCTIONNAME$";
  private static String PARAMETER_PLACEHOLDER = "$PARAMETER$";

  @Override
  public List<?> executeFunctionQuery(UriResourceFunction uriResourceFunction, JPAFunction jpaFunction,
      JPAEntityType returnType, EntityManager em) throws ODataApplicationException {

    String queryString = generateQueryString(jpaFunction);
    Query nq = em.createNativeQuery(queryString, returnType.getTypeClass());
    int count = 1;
    for (JPAFunctionParameter parameter : jpaFunction.getParameter()) {
      UriParameter uriParameter = findParameterByExternalName(parameter, uriResourceFunction.getParameters());
      Object value = getValue(uriResourceFunction.getFunction(), parameter, uriParameter.getText());
      nq.setParameter(count, value);
      count += 1;
    }
    List<?> nr = nq.getResultList();
    return nr;
  }

  private String generateQueryString(JPAFunction jpaFunction) {
    StringBuffer parameterList = new StringBuffer();
    String queryString = SELECT_BASE_PATTERN;

    queryString = queryString.replace(FUNC_NAME_PLACEHOLDER, jpaFunction.getDBName());
    for (int i = 1; i <= jpaFunction.getParameter().size(); i++) {
      parameterList.append(",");
      parameterList.append("?");
      parameterList.append(i);
    }
    parameterList.deleteCharAt(0);
    return queryString.replace(PARAMETER_PLACEHOLDER, parameterList.toString());
  }

  private UriParameter findParameterByExternalName(JPAFunctionParameter parameter, List<UriParameter> uriParameters)
      throws ODataApplicationException {
    for (UriParameter uriParameter : uriParameters) {
      if (uriParameter.getName().equals(parameter.getName()))
        return uriParameter;
    }
    throw new ODataApplicationException("Parameter not found " + parameter.getName(), HttpStatusCode.BAD_REQUEST
        .getStatusCode(), Locale.ENGLISH);
  }

  private Object getValue(EdmFunction edmFunction, JPAFunctionParameter parameter, String uriValue)
      throws ODataApplicationException {
    String value = uriValue.replaceAll("'", "");
    EdmParameter edmParam = edmFunction.getParameter(parameter.getName());
    try {
      return ((EdmPrimitiveType) edmParam.getType()).valueOfString(value, false, parameter.maxLength(),
          parameter.precision(), parameter.scale(), true, parameter.getType());
    } catch (EdmPrimitiveTypeException e) {
      throw new ODataApplicationException("Unable to convert parameter value " + uriValue, HttpStatusCode.BAD_REQUEST
          .getStatusCode(), Locale.ENGLISH, e);
    }
  }
}
