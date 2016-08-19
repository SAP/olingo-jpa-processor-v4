package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.MethodKind;

class JPAFunctionCallImp implements JPAFunctionCall {
  private final MethodKind methodCall;
  private final List<JPAOperator> parameters;
  private final JPAOperationConverter converter;

  public JPAFunctionCallImp(final JPAOperationConverter converter, final MethodKind methodCall,
      final List<JPAOperator> parameters) {
    super();
    this.methodCall = methodCall;
    this.parameters = parameters;
    this.converter = converter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAFunctionCall#get()
   */
  @Override
  public Object get() throws ODataApplicationException {
    return converter.convert(this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAFunctionCall#getFunction()
   */
  @Override
  public MethodKind getFunction() {
    return methodCall;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAFunctionCall#getParameter(int)
   */
  @Override
  public JPAOperator getParameter(final int index) {
    return parameters.get(index);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.olingo.jpa.processor.core.filter.JPAFunctionCall#noParameters()
   */
  @Override
  public int noParameters() {
    return parameters.size();
  }

}
