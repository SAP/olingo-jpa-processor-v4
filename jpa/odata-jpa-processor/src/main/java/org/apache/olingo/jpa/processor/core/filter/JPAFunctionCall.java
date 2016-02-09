package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

public class JPAFunctionCall implements JPAOperator {
  private final MethodKind methodCall;
  private final List<JPAOperator> parameters;
  private final JPAOperationConverter converter;

  public JPAFunctionCall(JPAOperationConverter converter, MethodKind methodCall, List<JPAOperator> parameters) {
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

  public JPAOperator getParameter(int i) {
    return parameters.get(i);
  }

  public int noParameters() {
    return parameters.size();
  }

}
