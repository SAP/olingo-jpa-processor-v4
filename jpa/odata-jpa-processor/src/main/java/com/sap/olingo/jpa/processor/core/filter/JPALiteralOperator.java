package com.sap.olingo.jpa.processor.core.filter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.query.ExpressionUtil;

public class JPALiteralOperator implements JPAOperator {
  private final Literal literal;
  private final OData odata;
  private final String literalText;

  public JPALiteralOperator(final OData odata, final Literal literal) {
    this(odata, literal, literal.getText());

  }

  private JPALiteralOperator(OData odata, Literal literal, String literalText) {
    this.literal = literal;
    this.odata = odata;
    this.literalText = literalText;
  }

  @Override
  public Object get() throws ODataApplicationException {
    final EdmPrimitiveType edmType = ((EdmPrimitiveType) literal.getType());
    try {

      final Class<?> defaultType = edmType.getDefaultType();
      final Constructor<?> c = defaultType.getConstructor(String.class);
      return c.newInstance(edmType.fromUriLiteral(literalText));
    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
        | EdmPrimitiveTypeException | InstantiationException | NoSuchMethodException | SecurityException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Converts a literal value into system type of attribute
   */
  public Object get(final JPAAttribute attribute) throws ODataApplicationException {
    return ExpressionUtil.convertValueOnAttribute(odata, attribute, literalText);
  }

  public Object get(JPAOperationResultParameter returnType) throws ODataApplicationException {
    return ExpressionUtil.convertValueOnFacet(odata, returnType, literalText);
  }

  public Object get(JPAParameter jpaParameter) throws ODataApplicationException {

    return ExpressionUtil.convertValueOnFacet(odata, jpaParameter, literalText);
  }

  Literal getLiteral() {
    return literal;
  }

  JPALiteralOperator clone(String prefix, String postfix) {
    return new JPALiteralOperator(odata, literal, "'" + prefix + literal.getText().replaceAll("'", "") + postfix + "'");
  }
}
