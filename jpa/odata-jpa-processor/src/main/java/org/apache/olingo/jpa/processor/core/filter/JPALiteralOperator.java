package org.apache.olingo.jpa.processor.core.filter;

import org.apache.olingo.commons.api.edm.EdmPrimitiveType;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPATypeConvertor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;

public class JPALiteralOperator implements JPAOperator {
  private final Literal literal;
  private final OData odata;

  public JPALiteralOperator(final OData odata, final Literal literal) {
    this.literal = literal;
    this.odata = odata;
  }

  @Override
  public Object get() {
    final EdmPrimitiveType edmType = ((EdmPrimitiveType) literal.getType());
    try {
      return edmType.fromUriLiteral(literal.getText());
    } catch (EdmPrimitiveTypeException e) {
      // TODO Error handling
      e.printStackTrace();
    }
    return null;
  }

  public Object get(final JPAAttribute attribute) {

    String value = null;
    try {
      final CsdlProperty edmProperty = (CsdlProperty) attribute.getProperty();
      final EdmPrimitiveTypeKind edmTypeKind = JPATypeConvertor.convertToEdmSimpleType(attribute);
      // TODO literal does not convert decimals without scale properly
      // EdmPrimitiveType edmType = ((EdmPrimitiveType) literal.getType());
      final EdmPrimitiveType edmType = odata.createPrimitiveTypeInstance(edmTypeKind);
      value = edmType.fromUriLiteral(literal.getText());
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

  Literal getLiteral() {
    return literal;
  }
}
