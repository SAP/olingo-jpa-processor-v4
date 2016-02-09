package org.apache.olingo.jpa.processor.core.filter;

import java.util.Locale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPAOperationConverter {
  private final CriteriaBuilder cb;

  public JPAOperationConverter(CriteriaBuilder cb) {
    super();
    this.cb = cb;
  }

  final public Expression<Boolean> convert(JPABooleanOperator jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case AND:
      return cb.and(jpaOperator.getLeft(), jpaOperator.getRight());
    case OR:
      return cb.or(jpaOperator.getLeft(), jpaOperator.getRight());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  // TODO check generics!
  @SuppressWarnings({ "unchecked", "rawtypes" })
  final public Expression<Boolean> convert(JPAComparisonOperator jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case EQ:
      return cb.equal(jpaOperator.getLeft(), jpaOperator.getRight());
    case NE:
      return cb.notEqual(jpaOperator.getLeft(), jpaOperator.getRight());
    case GE:
      return cb.greaterThanOrEqualTo(jpaOperator.getLeft(), jpaOperator.getRight());
    case GT:
      return cb.greaterThan(jpaOperator.getLeft(), jpaOperator.getRight());
    case LT:
      return cb.lessThan(jpaOperator.getLeft(), jpaOperator.getRight());
    case LE:
      return cb.lessThanOrEqualTo(jpaOperator.getLeft(), jpaOperator.getRight());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  final public Expression<Boolean> convert(JPAUnaryBooleanOperator jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case NOT:
      return cb.not(jpaOperator.getLeft());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  protected Predicate convertSpecific(JPABooleanOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.BAD_REQUEST.ordinal(), Locale.ENGLISH);
  }

  protected Expression<Boolean> convertSpecific(JPAExpressionOperator jpaOperator) throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ENGLISH);
  }

  protected Predicate convertSpecific(JPAUnaryBooleanOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.BAD_REQUEST.ordinal(), Locale.ENGLISH);
  }

}
