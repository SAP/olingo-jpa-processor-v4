package org.apache.olingo.jpa.processor.core.filter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;

import org.apache.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;
import org.apache.olingo.server.api.ODataApplicationException;

public class JPAOperationConverter {

  protected final CriteriaBuilder cb;
  private final JPAODataDatabaseOperations dbConverter;

  public JPAOperationConverter(CriteriaBuilder cb, JPAODataDatabaseOperations converterExtension) {
    super();
    this.cb = cb;
    this.dbConverter = converterExtension;
    this.dbConverter.setCriterialBuilder(cb);
  }

  @SuppressWarnings("unchecked")
  final public <T extends Number> Expression<T> convert(final JPAArithmeticOperator jpaOperator)
      throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case ADD:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.sum(jpaOperator.getLeft(cb), jpaOperator.getRightAsNumber(cb));
      else
        return (Expression<T>) cb.sum(jpaOperator.getLeft(cb), jpaOperator.getRightAsExpression());
    case SUB:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.diff(jpaOperator.getLeft(cb), jpaOperator.getRightAsNumber(cb));
      else
        return (Expression<T>) cb.diff(jpaOperator.getLeft(cb), jpaOperator.getRightAsExpression());
    case DIV:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.quot(jpaOperator.getLeft(cb), jpaOperator.getRightAsNumber(cb));
      else
        return (Expression<T>) cb.quot(jpaOperator.getLeft(cb), jpaOperator.getRightAsExpression());
    case MUL:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.prod(jpaOperator.getLeft(cb), jpaOperator.getRightAsNumber(cb));
      else
        return (Expression<T>) cb.prod(jpaOperator.getLeft(cb), jpaOperator.getRightAsExpression());
    case MOD:
      if (jpaOperator.getRight() instanceof JPALiteralOperator)
        return (Expression<T>) cb.mod(jpaOperator.getLeftAsIntExpression(), new Integer(jpaOperator.getRightAsNumber(cb)
            .toString()));
      else
        return (Expression<T>) cb.mod(jpaOperator.getLeftAsIntExpression(), jpaOperator.getRightAsIntExpression());
    default:
      return dbConverter.convert(jpaOperator);
    }
  }

  final public Expression<Boolean> convert(final JPABooleanOperatorImp jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case AND:
      return cb.and(jpaOperator.getLeft(), jpaOperator.getRight());
    case OR:
      return cb.or(jpaOperator.getLeft(), jpaOperator.getRight());
    default:
      return dbConverter.convert(jpaOperator);
    }
  }

  // TODO check generics!
  @SuppressWarnings({ "unchecked", "rawtypes" })
  final public Expression<Boolean> convert(final JPAComparisonOperatorImp jpaOperator)
      throws ODataApplicationException {
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
      return dbConverter.convert(jpaOperator);
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
      // OData defines start position in SUBSTRING as 0 (see
      // http://docs.oasis-open.org/odata/odata/v4.0/os/part2-url-conventions/odata-v4.0-os-part2-url-conventions.html#_Toc372793820)
      // SQL databases respectively use 1 as start position of a string

      final Expression<Integer> start = convertLiteralToExpression(jpaFunction, 1, 1);
      // final Integer start = new Integer(((JPALiteralOperator) jpaFunction.getParameter(1)).get().toString()) + 1;
      if (jpaFunction.noParameters() == 3) {
        final Expression<Integer> length = convertLiteralToExpression(jpaFunction, 2, 0);
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
      return dbConverter.convert(jpaFunction);
    }
  }

  final public Expression<Boolean> convert(final JPAUnaryBooleanOperatorImp jpaOperator)
      throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
    case NOT:
      return cb.not(jpaOperator.getLeft());
    default:
      return dbConverter.convert(jpaOperator);
    }
  }

  final public Expression<Long> convert(final JPAAggregationOperationImp jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getAggregation()) {
    case COUNT:
      return cb.count(jpaOperator.getPath());
    default:
      return dbConverter.convert(jpaOperator);
    }
  }

  @SuppressWarnings("unchecked")
  private Expression<Integer> convertLiteralToExpression(final JPAFunctionCall jpaFunction, final int parameterIndex,
      final int offset) throws ODataApplicationException {
    final JPAOperator parameter = jpaFunction.getParameter(parameterIndex);
    if (parameter instanceof JPAArithmeticOperatorImp) {
      if (offset != 0)
        return cb.sum((Expression<Integer>) jpaFunction.getParameter(parameterIndex).get(), offset);
      else
        return (Expression<Integer>) jpaFunction.getParameter(parameterIndex).get();
    } else
      return cb.literal(new Integer(parameter.get().toString()) + offset);
  }

}
