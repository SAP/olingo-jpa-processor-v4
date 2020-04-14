package com.sap.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEnumerationAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;

public final class JPAEnumerationOperator implements JPAEnumerationBasedOperator {

  private final JPAEnumerationAttribute jpaAttribute;
  private final List<String> value;

  JPAEnumerationOperator(JPAEnumerationAttribute jpaEnumerationAttribute, List<String> enumValues) {
    super();
    this.jpaAttribute = jpaEnumerationAttribute;
    this.value = enumValues;
  }

  /**
   * Returns either an instance of an enumeration or an array of enumerations. This is sufficient for <i>eq</i>,
   * <i>ne</i> and <i>has</i> operations, but will not work with any operation that requires a <code>comparable</code>
   * like <i>gt</i>. As of now such operations are already blocked by Olingo in ExpressionParser.checkType().<br>
   * In case in the future these operations shall be supported this method has to return an array of
   * <code>comparable</code>, which goes with an incompatible change of annotation EdmEnumeration, as converters are
   * required using such an array.
   */
  @Override
  public Object get() throws ODataApplicationException {
    try {
      return jpaAttribute.convert(value);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAEnumerationBasedOperator#getValue()
   */
  @Override
  public Number getValue() throws ODataJPAFilterException {
    try {
      return jpaAttribute.valueOf(value);
    } catch (ODataJPAModelException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public String getName() {
    return "";
  }
}
