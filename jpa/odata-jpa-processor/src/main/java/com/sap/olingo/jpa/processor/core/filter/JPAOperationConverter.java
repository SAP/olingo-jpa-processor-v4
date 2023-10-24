package com.sap.olingo.jpa.processor.core.filter;

import java.util.function.BiFunction;
import java.util.function.Function;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.queryoption.expression.UnaryOperatorKind;

import com.sap.olingo.jpa.processor.core.database.JPAODataDatabaseOperations;

public class JPAOperationConverter {

  protected final CriteriaBuilder cb;
  private final JPAODataDatabaseOperations dbConverter;

  public JPAOperationConverter(final CriteriaBuilder cb, final JPAODataDatabaseOperations converterExtension) {
    super();
    this.cb = cb;
    this.dbConverter = converterExtension;
    this.dbConverter.setCriterialBuilder(cb);
  }

  public final Expression<Long> convert(final JPAAggregationOperationImp jpaOperator) throws ODataApplicationException {

    if (jpaOperator.getAggregation() == JPAFilterAggregationType.COUNT)
      return cb.count(jpaOperator.getPath());
    return dbConverter.convert(jpaOperator);

  }

  @SuppressWarnings("unchecked")
  public final <T extends Number> Expression<T> convert(final JPAArithmeticOperator jpaOperator)
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
          return (Expression<T>) cb.mod(jpaOperator.getLeftAsIntExpression(), Integer.valueOf(jpaOperator
              .getRightAsNumber(cb).toString()));
        else
          return (Expression<T>) cb.mod(jpaOperator.getLeftAsIntExpression(), jpaOperator.getRightAsIntExpression());

      default:
        return dbConverter.convert(jpaOperator);
    }
  }

  public final Expression<Boolean> convert(final JPABooleanOperatorImp jpaOperator) throws ODataApplicationException {
    switch (jpaOperator.getOperator()) {
      case AND:
        return cb.and(jpaOperator.getLeft(), jpaOperator.getRight());
      case OR:
        return cb.or(jpaOperator.getLeft(), jpaOperator.getRight());
      default:
        return dbConverter.convert(jpaOperator);
    }
  }

  @SuppressWarnings({ "unchecked" })
  public final Expression<Boolean> convert(@SuppressWarnings("rawtypes") final JPAComparisonOperatorImp jpaOperator)
      throws ODataApplicationException {

    switch (jpaOperator.getOperator()) {
      case EQ:
        return equalExpression((left, right) -> (cb.equal(left, right)), (left, right) -> (cb.equal(left, right)),
            left -> (cb.isNull(left)),
            jpaOperator);
      case NE:
        return equalExpression((left, right) -> (cb.notEqual(left, right)), (left, right) -> (cb.notEqual(left, right)),
            left -> (cb.isNotNull(left)),
            jpaOperator);
      case GE:
        return comparisonExpression((left, right) -> (cb.greaterThanOrEqualTo(left, right)), (left, right) -> (cb
            .greaterThanOrEqualTo(left,
                right)), jpaOperator);
      case GT:
        return comparisonExpression((left, right) -> (cb.greaterThan(left, right)), (left, right) -> (cb.greaterThan(
            left, right)), jpaOperator);
      case LT:
        return comparisonExpression((left, right) -> (cb.lessThan(left, right)), (left, right) -> (cb.lessThan(left,
            right)), jpaOperator);
      case LE:
        return comparisonExpression((left, right) -> (cb.lessThanOrEqualTo(left, right)), (left, right) -> (cb
            .lessThanOrEqualTo(left, right)),
            jpaOperator);
      default:
        return dbConverter.convert(jpaOperator);
    }

  }

  @SuppressWarnings("unchecked")
  public Expression<?> convert(final JPAMethodCall jpaFunction) throws ODataApplicationException {
    switch (jpaFunction.getFunction()) {
      // First String functions
      // TODO Escape like functions
      case LENGTH:
        return cb.length((Expression<String>) (jpaFunction.getParameter(0).get()));
      case CONTAINS:
        if (jpaFunction.getParameter(1) instanceof JPALiteralOperator) {
          return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()),
              buildLikeLiteral(jpaFunction, "%", "%").toString());
        } else {
          return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()),
              (Expression<String>) ((JPAMethodCall) jpaFunction.getParameter(1)).get("%", "%"));
        }
      case ENDSWITH:
        if (jpaFunction.getParameter(1) instanceof JPALiteralOperator) {
          return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()),
              buildLikeLiteral(jpaFunction, "%", "").toString());
        } else {
          return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()),
              (Expression<String>) ((JPAMethodCall) jpaFunction.getParameter(1)).get("%", ""));
        }
      case STARTSWITH:
        if (jpaFunction.getParameter(1) instanceof JPALiteralOperator) {
          return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()),
              buildLikeLiteral(jpaFunction, "", "%").toString());
        } else {
          return cb.like((Expression<String>) (jpaFunction.getParameter(0).get()),
              (Expression<String>) ((JPAMethodCall) jpaFunction.getParameter(1)).get("", "%"));
        }
      case INDEXOF:
        final String searchString = ((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get());
        return cb.locate((Expression<String>) (jpaFunction.getParameter(0).get()), searchString);
      case SUBSTRING:
        // OData defines start position in SUBSTRING as 0 (see
        // http://docs.oasis-open.org/odata/odata/v4.0/os/part2-url-conventions/odata-v4.0-os-part2-url-conventions.html#_Toc372793820)
        // SQL databases respectively use 1 as start position of a string

        final Expression<Integer> start = convertLiteralToExpression(jpaFunction, 1, 1);
        if (jpaFunction.noParameters() == 3) {
          final Expression<Integer> length = convertLiteralToExpression(jpaFunction, 2, 0);
          return cb.substring((Expression<String>) (jpaFunction.getParameter(0).get()), start, length);
        } else {
          return cb.substring((Expression<String>) (jpaFunction.getParameter(0).get()), start);
        }

      case TOLOWER:
//      // TODO Locale!! and inverted parameter sequence
        if (jpaFunction.getParameter(0).get() instanceof String)
          return cb.literal(jpaFunction.getParameter(0).get().toString().toLowerCase());
        return cb.lower((Expression<String>) (jpaFunction.getParameter(0).get()));
      case TOUPPER:
        if (jpaFunction.getParameter(0).get() instanceof String)
          return cb.literal(jpaFunction.getParameter(0).get().toString().toUpperCase());
        return cb.upper((Expression<String>) (jpaFunction.getParameter(0).get()));
      case TRIM:
        return cb.trim((Expression<String>) (jpaFunction.getParameter(0).get()));
      case CONCAT:
        if (jpaFunction.getParameter(0).get() instanceof final String parameter0)
          return cb.concat(parameter0, (Expression<String>) (jpaFunction.getParameter(1).get()));
        if (jpaFunction.getParameter(1).get() instanceof final String parameter1)
          return cb.concat((Expression<String>) (jpaFunction.getParameter(0).get()), parameter1);
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

  private StringBuilder buildLikeLiteral(final JPAMethodCall jpaFunction, final String prefix,
      final String postfix) throws ODataApplicationException {

    final StringBuilder contains = new StringBuilder();
    contains.append(prefix);
    contains.append((String) ((JPALiteralOperator) jpaFunction.getParameter(1)).get());
    contains.append(postfix);
    return contains;
  }

  public final Expression<Boolean> convert(final JPAUnaryBooleanOperatorImp jpaOperator)
      throws ODataApplicationException {

    if (jpaOperator.getOperator() == UnaryOperatorKind.NOT)
      return cb.not(jpaOperator.getLeft());
    return dbConverter.convert(jpaOperator);

  }

  @SuppressWarnings({ "unchecked" })
  private <Y extends Comparable<? super Y>> Expression<Boolean> comparisonExpression(
      final BiFunction<Expression<? extends Y>, Expression<? extends Y>, Expression<Boolean>> allExpressionFunction,
      final BiFunction<Expression<? extends Y>, Y, Expression<Boolean>> expressionObjectFunction,
      final JPAComparisonOperator<? extends Y> jpaOperator) throws ODataApplicationException {

    if (jpaOperator.getRight() instanceof JPAPrimitiveTypeOperator)
      return expressionObjectFunction.apply(jpaOperator.getLeft(), (Y) jpaOperator.getRightAsComparable());
    else
      return allExpressionFunction.apply(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
  }

  @SuppressWarnings("unchecked")
  private Expression<Integer> convertLiteralToExpression(final JPAMethodCall jpaFunction, final int parameterIndex,
      final int offset) throws ODataApplicationException {
    final JPAOperator parameter = jpaFunction.getParameter(parameterIndex);
    if (parameter instanceof JPAArithmeticOperatorImp) {
      if (offset != 0)
        return cb.sum((Expression<Integer>) jpaFunction.getParameter(parameterIndex).get(), offset);
      else
        return (Expression<Integer>) jpaFunction.getParameter(parameterIndex).get();
    } else {
      return cb.literal(Integer.valueOf(parameter.get().toString()) + offset);
    }
  }

  private Expression<Boolean> equalExpression(
      final BiFunction<Expression<?>, Expression<?>, Expression<Boolean>> allExpressionFunction,
      final BiFunction<Expression<?>, Object, Expression<Boolean>> expressionObjectFunction,
      final Function<Expression<?>, Expression<Boolean>> nullFunction,
      final JPAComparisonOperator<?> jpaOperator) throws ODataApplicationException {

    if (jpaOperator.getRight() instanceof JPAPrimitiveTypeOperator) {
      if (((JPAPrimitiveTypeOperator) jpaOperator.getRight()).isNull())
        return nullFunction.apply(jpaOperator.getLeft());
      else if (jpaOperator.getRight() instanceof JPAEnumerationOperator)
        return expressionObjectFunction.apply(jpaOperator.getLeft(), ((JPAOperator) jpaOperator.getRight()).get());
      else
        return expressionObjectFunction.apply(jpaOperator.getLeft(), jpaOperator.getRightAsComparable());
    } else {
      return allExpressionFunction.apply(jpaOperator.getLeft(), jpaOperator.getRightAsExpression());
    }
  }
}
