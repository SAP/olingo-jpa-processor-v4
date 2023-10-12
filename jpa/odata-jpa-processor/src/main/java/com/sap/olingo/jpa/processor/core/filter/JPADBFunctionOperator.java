package com.sap.olingo.jpa.processor.core.filter;

import static com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FILTER;

import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import org.apache.olingo.commons.api.edm.EdmType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitException;
import org.apache.olingo.server.api.uri.queryoption.expression.ExpressionVisitor;
import org.apache.olingo.server.api.uri.queryoption.expression.Literal;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPADataBaseFunction;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOperationResultParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAParameter;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.impl.JPATypeConverter;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAFilterException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAProcessorException;

/**
 * Handle OData Functions that are implemented as user defined data base functions. This will be mapped
 * to JPA criteria builder function().
 *
 * @author Oliver Grande
 *
 */
final class JPADBFunctionOperator implements JPAOperator {
  private final JPADataBaseFunction jpaFunction;
  private final JPAVisitor visitor;
  private final List<UriParameter> uriParams;

  public JPADBFunctionOperator(final JPAVisitor jpaVisitor, final List<UriParameter> uriParams,
      final JPADataBaseFunction jpaFunction) {

    super();
    this.uriParams = uriParams;
    this.visitor = jpaVisitor;
    this.jpaFunction = jpaFunction;
  }

  @Override
  public Expression<?> get() throws ODataApplicationException {

    if (jpaFunction.getResultParameter().isCollection()) {
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FUNCTION_COLLECTION,
          HttpStatusCode.NOT_IMPLEMENTED);
    }

    if (!JPATypeConverter.isScalarType(
        jpaFunction.getResultParameter().getType())) {
      throw new ODataJPAFilterException(ODataJPAFilterException.MessageKeys.NOT_SUPPORTED_FUNCTION_NOT_SCALAR,
          HttpStatusCode.NOT_IMPLEMENTED);
    }

    final CriteriaBuilder cb = visitor.getCriteriaBuilder();
    List<JPAParameter> parameters;
    try {
      parameters = jpaFunction.getParameter();
    } catch (final ODataJPAModelException e) {
      throw new ODataJPAProcessorException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
    final Expression<?>[] jpaParameter = new Expression<?>[parameters.size()];
    for (int i = 0; i < parameters.size(); i++) {
      // a. $it/Area b. Area c. 10000
      final UriParameter p = findUriParameter(parameters.get(i));

      if (p != null && p.getText() != null) {
        final JPALiteralOperator operator = new JPALiteralOperator(visitor.getOData(), new ParameterLiteral(p
            .getText()));
        jpaParameter[i] = cb.literal(operator.get(parameters.get(i)));
      } else if (p != null && p.getExpression() != null) {
        try {
          jpaParameter[i] = (Expression<?>) p.getExpression().accept(visitor).get();
        } catch (final ExpressionVisitException e) {
          throw new ODataJPAFilterException(e, HttpStatusCode.NOT_IMPLEMENTED);
        }
      } else {
        throw new ODataJPAFilterException(NOT_SUPPORTED_FILTER, HttpStatusCode.NOT_IMPLEMENTED);
      }
    }
    return cb.function(jpaFunction.getDBName(), jpaFunction.getResultParameter().getType(), jpaParameter);
  }

  private UriParameter findUriParameter(final JPAParameter jpaFunctionParam) {
    for (final UriParameter uriParam : uriParams) {
      if (uriParam.getName().equals(jpaFunctionParam.getName())) {
        return uriParam;
      }
    }
    return null;
  }

  public JPAOperationResultParameter getReturnType() {
    return jpaFunction.getResultParameter();
  }

  @Override
  public String getName() {
    return jpaFunction.getDBName();
  }

  private record ParameterLiteral(String text) implements Literal {

    @Override
    public <T> T accept(final ExpressionVisitor<T> visitor) throws ExpressionVisitException, ODataApplicationException {
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
