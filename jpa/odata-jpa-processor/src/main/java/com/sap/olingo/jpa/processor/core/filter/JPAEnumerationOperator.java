package com.sap.olingo.jpa.processor.core.filter;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

public final class JPAEnumerationOperator implements JPAPrimitiveTypeOperator {

  private final JPAEnumerationAttribute jpaAttribute;
  private final String value;

  JPAEnumerationOperator(JPAEnumerationAttribute jpaEnumerationAttribute, String value) {
    super();
    this.jpaAttribute = jpaEnumerationAttribute;
    this.value = value;
  }

  @Override
  public Object get() throws ODataApplicationException {
    try {
      return jpaAttribute.enumOf(value);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  public Number getValue() throws ODataJPAFilterException {
    try {
      return jpaAttribute.valueOf(value);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
