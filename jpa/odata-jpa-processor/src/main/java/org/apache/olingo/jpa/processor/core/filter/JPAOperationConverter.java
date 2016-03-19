package org.apache.olingo.jpa.processor.core.filter;

import java.util.Locale;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPAOperationConverter {
  protected final CriteriaBuilder cb;

  public JPAOperationConverter(final CriteriaBuilder cb) {
    super();
    this.cb = cb;
  }

  @SuppressWarnings("unchecked")
  final public <T extends Number> Expression<T> convert(final JPAArithmeticOperator jpaOperator)
      throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case ADD:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.sum(jpaOperator.getLeft(), jpaOperator.getRightAsNumber());
      else
        return (Expression<T>) cb.sum(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case SUB:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.diff(jpaOperator.getLeft(), jpaOperator.getRightAsNumber());
      else
        return (Expression<T>) cb.diff(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case DIV:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.quot(jpaOperator.getLeft(), jpaOperator.getRightAsNumber());
      else
        return (Expression<T>) cb.quot(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case MUL:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.prod(jpaOperator.getLeft(), jpaOperator.getRightAsNumber());
      else
        return (Expression<T>) cb.prod(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case MOD:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.mod(jpaOperator.getLeftAsIntExpression(), new Integer(jpaOperator.getRightAsNumber()
            .toString()));
      else
        return (Expression<T>) cb.mod(jpaOperator.getLeftAsIntExpression(), jpaOperator.getRightAsIntExpression());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  final public Expression<Boolean> convert(final JPABooleanOperator jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case AND:
      return cb.and(jpaOperator.getLeft(), jpaOperator.getRight());
    case OR:
      return cb.or(jpaOperator.getLeft(), jpaOperator.getRight());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  final public Expression<Boolean> convert(final JPAExistsOperation jpaOperator) throws ODataApplicationException {
    return cb.exists(jpaOperator.getSubQuery());

  }

  // TODO check generics!
  @SuppressWarnings({ "unchecked", "rawtypes" })
  final public Expression<Boolean> convert(final JPAComparisonOperator jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case EQ:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return cb.equal(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
      else
        return cb.equal(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case NE:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return cb.notEqual(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
      else
        return cb.notEqual(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case GE:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return cb.greaterThanOrEqualTo(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
      else
        return cb.greaterThanOrEqualTo(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case GT:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return cb.greaterThan(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
      else
        return cb.greaterThan(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case LT:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return cb.lessThan(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
      else
        return cb.lessThan(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    case LE:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return cb.lessThanOrEqualTo(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
      else
        return cb.lessThanOrEqualTo(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  @SuppressWarnings("unchecked")
  public Object convert(final JPAFunctionCall jpaFunction) throws ODataApplicationException {
    switch (jpaFunction.getFunction()) {
    // First String functions
    case LENGTH:
      return cb.length((Expression<String>) (jpaFunction.getParameter(0).get()));
    case CONTAINS:
      final StringBuffer contains = new StringBuffer();
      contains.append('%');
      contains.append((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get());
      contains.append('%');
      return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()), contains.toString());
    case ENDSWITH:
      final StringBuffer ends = new StringBuffer();
      ends.append('%');
      ends.append((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get());
      return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()), ends.toString());
    case STARTSWITH:
      final StringBuffer starts = new StringBuffer();
      starts.append((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get());
      starts.append('%');
      return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()), starts.toString());
    case INDEXOF:
      final String searchString = ((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get());
      return cb.locate((Expression<String>) (jpaFunction.getParameter(0).get()), searchString);
    case SUBSTRING:
      // Substring worked fine with H2 and HANA, but had problems with HSQLDB
      final Integer start = new Integer(((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get()));
      if (jpaFunction.noParameters() == 3) {
        final Integer length = new Integer(((String) ((JPALiteralOperator) jpaFunction.getParameter(2)).get()));
        return cb.substring((Expression<String>) (jpaFunction.getParameter(0).get()), start, length);
      } else
        return cb.substring((Expression<String>) (jpaFunction.getParameter(0).get()), start);

    case TOLOWER:
      // TODO Locale!! and inverted parameter sequence
      if (jpaFunction.getParameter(0).get() instanceof String)
        return jpaFunction.getParameter(0).get().toString().toLowerCase();
      return cb.lower((Expression<String>) (jpaFunction.getParameter(0).get()));
    case TOUPPER:
      if (jpaFunction.getParameter(0).get() instanceof String)
        return jpaFunction.getParameter(0).get().toString().toUpperCase();
      return cb.upper((Expression<String>) (jpaFunction.getParameter(0).get()));
    case TRIM:
      return cb.trim((Expression<String>) (jpaFunction.getParameter(0).get()));
    case CONCAT:
      if (jpaFunction.getParameter(0).get() instanceof String)
        return cb.concat((String) jpaFunction.getParameter(0).get(), (Expression<String>) (jpaFunction.getParameter(1)
            .get()));
      if (jpaFunction.getParameter(1).get() instanceof String)
        return cb.concat((Expression<String>) (jpaFunction.getParameter(0).get()), (String) jpaFunction.getParameter(1)
            .get());
      else
        return cb.concat((Expression<String>) (jpaFunction.getParameter(0).get()),
            (Expression<String>) (jpaFunction.getParameter(1).get()));
      // Second Date-Time functions
    case NOW:
      return cb.currentTimestamp();
    default:
      return convertSpecific(jpaFunction);
    }
  }

  final public Expression<Boolean> convert(final JPAUnaryBooleanOperator jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case NOT:
      return cb.not(jpaOperator.getLeft());
    default:
      return convertSpecific(jpaOperator);
    }
  }

  protected <T extends Number> Expression<T> convertSpecific(final JPAArithmeticOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  protected Predicate convertSpecific(final JPABooleanOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  protected Expression<Boolean> convertSpecific(final JPAExpressionOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  protected Object convertSpecific(final JPAFunctionCall jpaFunction) throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaFunction.getFunction() + " not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

  protected Predicate convertSpecific(final JPAUnaryBooleanOperator jpaOperator)
      throws ODataApplicationException {
    throw new ODataApplicationException("Operator " + jpaOperator.getOperator() + " not supported",
        HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ENGLISH);
  }

}
