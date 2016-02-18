package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

public class JPAFunctionCall implements JPAOperator {
  private final MethodKind methodCall;
  private final List<JPAOperator> parameters;
  private final JPAOperationConverter converter;

  public JPAFunctionCall(final JPAOperationConverter converter, final MethodKind methodCall,
      final List<JPAOperator> parameters) {
    super();
    this.methodCall = methodCall;
    this.parameters = parameters;
    this.converter = converter;
  }

  @Override
  public Object get() throws ODataApplicationException {
    return converter.convert(this);
  }

  public MethodKind getFunction() {
    return methodCall;
  }

  public JPAOperator getParameter(final int index) {
    return parameters.get(index);
  }

  public int noParameters() {
    return parameters.size();
  }

}
