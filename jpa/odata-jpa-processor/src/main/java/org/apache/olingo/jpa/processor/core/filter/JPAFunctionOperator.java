package org.apache.olingo.jpa.processor.core.filter;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunction;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.api.JPAFunctionResultParameter;
import org.apache.olingo.jpa.metadata.core.edm.mapper.impl.JPATypeConvertor;
import org.apache.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;

/**
 * Handle OData Functions that are implemented e.g. as user defined functions data base functions. This will be mapped
 * to JPA criteria builder function().
 * 
 * @author Oliver Grande
 *
 */
public class JPAFunctionOperator implements JPAOperator {
  private final JPAFunction jpaFunction;
  private final JPAVisitor visitor;
  private final List<UriParameter> uriParams;

  public JPAFunctionOperator(JPAVisitor jpaVisitor, List<UriParameter> uriParams, JPAFunction jpaFunction) {

    super();
    this.uriParams = uriParams;
    this.visitor = jpaVisitor;
    this.jpaFunction = jpaFunction;
  }

  @Override
  public Expression<?> get() throws ODataApplicationException {
    final CriteriaBuilder cb = visitor.getCriteriaBuilder();
    final List<JPAFunctionParameter> parameters = jpaFunction.getParameter();
    final Expression<?>[] jpaParameter = new Expression<?>[parameters.size()];

    if (jpaFunction.getResultParameter().isCollection()) {
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FUNCTION_COLLECTION,
          HttpStatusCode.NOT_IMPLEMENTED);
    }

    if (!JPATypeConvertor.isScalarType(
        jpaFunction.getResultParameter().getType())) {
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FUNCTION_NOT_SCALAR,
          HttpStatusCode.NOT_IMPLEMENTED);
    }
    for (int i = 0; i < parameters.size(); i++) {
      // a. $it/Area b. Area c. 10000
      UriParameter p = findUriParameter(parameters.get(i));

      if (p.getText() != null) {
        JPALiteralOperator operator = new JPALiteralOperator(visitor.getOdata(), new ParameterLiteral(p.getText()));
        jpaParameter[i] = cb.literal(operator.get(parameters.get(i)));
      } else {
        try {
          jpaParameter[i] = (Expression<?>) p.getExpression().accept(visitor).get();
        } catch (ExpressionVisitException e) {
          throw new ODataJPAFilterException(e, HttpStatusCode.NOT_IMPLEMENTED);
        }
      }
    }
    return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
  }

  private UriParameter findUriParameter(JPAFunctionParameter jpaFunctionParam) {
    for (UriParameter uriParam : uriParams) {
      if (uriParam.getName().equals(jpaFunctionParam.getName())) {
        return uriParam;
      }
    }
    return null;
  }

  public JPAFunctionResultParameter getReturnType() {
    return jpaFunction.getResultParameter();
  }

  private class ParameterLiteral implements Literal {

    public ParameterLiteral(String text) {
      super();
      this.text = text;
    }

    private final String text;

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
      return null;
    }

    @Override
    public String getText() {
      return text;
    }

    @Override
    public EdmType getType() {
      return null;
    }
  }
}
