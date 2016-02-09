package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;

public class JPALiteralOperator implements JPAOperator {
  private final Literal literal;

  public JPALiteralOperator(Literal literal) {
    this.literal = literal;
  }

  @Override
  public Object get() {
    return null;
  }

  public Object get(JPAAttribute attribute) {
    EdmPrimitiveType edmType = ((EdmPrimitiveType) literal.getType());
    String value = null;
    try {
      value = edmType.fromUriLiteral(literal.getText());
      CsdlProperty edmProperty = (CsdlProperty) attribute.getProperty();
      return edmType.valueOfString(value, edmProperty.isNullable(), edmProperty.getMaxLength(),
          edmProperty.getPrecision(), edmProperty.getScale(), true, attribute.getType());
    } catch (EdmPrimitiveTypeException e) {
      // TODO Error handling
      e.printStackTrace();
    } catch (ODataJPAModelException e) {
      // TODO Error handling
      e.printStackTrace();
    }
    return value;
  }
}
