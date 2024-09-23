package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.commons.api.edm.EdmKeyPropertyRef;
import org.apache.olingo.commons.core.edm.primitivetype.EdmString;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;

record JPAUriParameterImpl(EdmKeyPropertyRef propertyReference, String value) implements UriParameter {

  @Override
  public String getAlias() {
    return propertyReference.getAlias();
  }

  @Override
  public String getText() {
    if (propertyReference.getProperty().getType() instanceof EdmString)
      return "'" + value + "'";
    return value;
  }

  @Override
  public Expression getExpression() {
    return null;
  }

  @Override
  public String getName() {
    return propertyReference.getName();
  }

  @Override
  public String getReferencedProperty() {
    return null;
  }

}
