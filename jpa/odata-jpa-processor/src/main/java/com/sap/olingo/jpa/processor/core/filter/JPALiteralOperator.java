package com.sap.olingo.jpa.processor.core.filter;

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

public class JPALiteralOperator implements JPAPrimitiveTypeOperator {
  private final Literal literal;
  private final OData odata;
  private final String literalText;

  public JPALiteralOperator(final OData odata, final Literal literal) {
    this(odata, literal, literal.getText());
  }

  private JPALiteralOperator(final OData odata, final Literal literal, final String literalText) {
    this.literal = literal;
    this.odata = odata;
    this.literalText = literalText;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.processor.core.filter.JPAPrimitiveTypeOperator#get()
   */
  @Override
  public Object get() throws ODataApplicationException {
    final EdmPrimitiveType edmType = ((EdmPrimitiveType) literal.getType());

    try {
      final Object value = edmType.valueOfString(literalText, true, null, null, null, true, edmType.getDefaultType());
      if (value instanceof String)
        return ((String) value).replace("'", "");
      return value;
    } catch (final EdmPrimitiveTypeException e) {
      throw new ODataJPAFilterException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Converts a literal value into system type of attribute
   */
  public Object get(final JPAAttribute attribute) throws ODataApplicationException {
    return ExpressionUtil.convertValueOnAttribute(odata, attribute, literalText);
  }

  public Object get(final JPAOperationResultParameter returnType) throws ODataApplicationException {
    return ExpressionUtil.convertValueOnFacet(odata, returnType, literalText);
  }

  public Object get(final JPAParameter jpaParameter) throws ODataApplicationException {

    return ExpressionUtil.convertValueOnFacet(odata, jpaParameter, literalText);
  }

  @Override
  public boolean isNull() {
    return literal.getText().equals("null");
  }

  JPALiteralOperator clone(final String prefix, final String postfix) {
    return new JPALiteralOperator(odata, literal, "'" + prefix + literal.getText().replace("'", "") + postfix + "'");
  }

  Literal getLiteral() {
    return literal;
  }

  @Override
  public String getName() {
    return literal.getText();
  }
}
