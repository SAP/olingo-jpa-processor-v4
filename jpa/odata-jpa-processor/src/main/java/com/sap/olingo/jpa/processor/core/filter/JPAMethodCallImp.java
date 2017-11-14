package com.sap.olingo.jpa.processor.core.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

class JPAMethodCallImp implements JPAMethodCall {
  private final MethodKind methodCall;
  private final List<JPAOperator> parameters;
  private final JPAOperationConverter converter;

  public JPAMethodCallImp(final JPAOperationConverter converter, final MethodKind methodCall,
      final List<JPAOperator> parameters) {
    super();
    this.methodCall = methodCall;
    this.parameters = parameters;
    this.converter = converter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFunctionCall#get()
   */
  @Override
  public Object get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFunctionCall#get(String prefix, String suffix)
   */
  @Override
  public Object get(final String prefix, final String suffix) throws ODataApplicationException {
    final List<JPAOperator> paramCopy = new ArrayList<>(parameters);
    if (!parameters.isEmpty() && parameters.get(0) instanceof JPALiteralOperator) {
      parameters.add(((JPALiteralOperator) parameters.get(0)).clone(prefix, suffix));
      parameters.remove(0);
    }
    Expression<?> result = converter.convert(this);
    Collections.copy(parameters, paramCopy);
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFunctionCall#getFunction()
   */
  @Override
  public MethodKind getFunction() {
    return methodCall;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFunctionCall#getParameter(int)
   */
  @Override
  public JPAOperator getParameter(final int index) {
    return parameters.get(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.sap.olingo.jpa.processor.core.filter.JPAFunctionCall#noParameters()
   */
  @Override
  public int noParameters() {
    return parameters.size();
  }

}
