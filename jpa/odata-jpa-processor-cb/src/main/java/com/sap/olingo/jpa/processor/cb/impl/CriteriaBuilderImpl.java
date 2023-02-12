package com.sap.olingo.jpa.processor.cb.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.persistence.Tuple;
import javax.persistence.criteria.CollectionJoin;
import javax.persistence.criteria.CompoundSelection;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.MapJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaQuery;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;
import com.sap.olingo.jpa.processor.cb.impl.PredicateImpl.BinaryExpressionPredicate.Operation;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;

class CriteriaBuilderImpl implements ProcessorCriteriaBuilder { // NOSONAR

  private final JPAServiceDocument sd;
  private final ParameterBuffer parameter;

  CriteriaBuilderImpl(final JPAServiceDocument sd, final ParameterBuffer parameterBuffer) {
    this.sd = sd;
    this.parameter = parameterBuffer;
  }

  /**
   * Create an expression that returns the absolute value
   * of its argument.
   * @param x expression
   * @return absolute value
   */
  @Override
  public <N extends Number> Expression<N> abs(@Nonnull final Expression<N> x) {
    throw new NotImplementedException();
  }

  /**
   * Create an all expression over the subquery results.
   * @param subquery subquery
   * @return all expression
   */
  @Override
  public <Y> Expression<Y> all(@Nonnull final Subquery<Y> subquery) {
    return new ExpressionImpl.SubQuery<>(subquery, SqlSubQuery.ALL);
  }

  @Override
  public Predicate and(final Expression<Boolean> x, final Expression<Boolean> y) {
    return new PredicateImpl.AndPredicate(x, y);
  }

  @Override
  public Predicate and(final Predicate... restrictions) {
    return PredicateImpl.and(restrictions);
  }

  /**
   * Create an any expression over the subquery results.
   * This expression is equivalent to a <code>some</code> expression.
   * @param subquery subquery
   * @return any expression
   */
  @Override
  public <Y> Expression<Y> any(@Nonnull final Subquery<Y> subquery) {
    return new ExpressionImpl.SubQuery<>(subquery, SqlSubQuery.ANY);
  }

  /**
   * Create an array-valued selection item.
   * @param selections selection items
   * @return array-valued compound selection
   * @throws IllegalArgumentException if an argument is a
   * tuple- or array-valued selection item
   */
  @Override
  public CompoundSelection<Object[]> array(@Nonnull final Selection<?>... selections) {
    throw new NotImplementedException();
  }

  /**
   * Create an ordering by the ascending value of the expression.
   * @param x expression used to define the ordering
   * @return ascending ordering corresponding to the expression
   */
  @Override
  public Order asc(@Nonnull final Expression<?> x) {
    return new OrderImpl(true, Objects.requireNonNull((SqlConvertible) x));
  }

  /**
   * Create an aggregate expression applying the avg operation.
   * @param x expression representing input value to avg operation
   * @return avg expression
   */
  @Override
  public <N extends Number> Expression<Double> avg(@Nonnull final Expression<N> x) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate for testing whether the first argument is
   * between the second and third arguments in value.
   * @param v expression
   * @param x expression
   * @param y expression
   * @return between predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate between(@Nonnull final Expression<? extends Y> v,
      @Nonnull final Expression<? extends Y> x, @Nonnull final Expression<? extends Y> y) {
    return new PredicateImpl.BetweenExpressionPredicate((ExpressionImpl<?>) v, x, y);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * between the second and third arguments in value.
   * @param v expression
   * @param x value
   * @param y value
   * @return between predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate between(@Nonnull final Expression<? extends Y> v,
      @Nonnull final Y x, @Nonnull final Y y) {
    return between(v, literal(x, v), literal(y, v));
  }

  /**
   * Create a coalesce expression.
   * @return coalesce expression
   */
  @Override
  public <T> Coalesce<T> coalesce() {
    return new ExpressionImpl.CoalesceExpression<>();
  }

  /**
   * Create an expression that returns null if all its arguments
   * evaluate to null, and the value of the first non-null argument
   * otherwise.
   * @param x expression
   * @param y expression
   * @return coalesce expression
   */
  @Override
  public <Y> Expression<Y> coalesce(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Expression<? extends Y> y) {
    return new ExpressionImpl.CoalesceExpression<Y>().value(x).value(y);
  }

  /**
   * Create an expression that returns null if all its arguments
   * evaluate to null, and the value of the first non-null argument
   * otherwise.
   * @param x expression
   * @param y value
   * @return coalesce expression
   */
  @Override
  public <Y> Expression<Y> coalesce(@Nonnull final Expression<? extends Y> x, @Nonnull final Y y) {
    return new ExpressionImpl.CoalesceExpression<Y>().value(x).value(literal(y));
  }

  /**
   * Create an expression for string concatenation.
   * @param x string expression
   * @param y string expression
   * @return expression corresponding to concatenation
   */
  @Override
  public Expression<String> concat(@Nonnull final Expression<String> x, @Nonnull final Expression<String> y) {
    return new ExpressionImpl.ConcatExpression(x, y);
  }

  /**
   * Create an expression for string concatenation.
   * @param x string expression
   * @param y string
   * @return expression corresponding to concatenation
   */
  @Override
  public Expression<String> concat(@Nonnull final Expression<String> x, @Nonnull final String y) {
    return new ExpressionImpl.ConcatExpression(x, literal(y));
  }

  /**
   * Create an expression for string concatenation.
   * @param x string
   * @param y string expression
   * @return expression corresponding to concatenation
   */
  @Override
  public Expression<String> concat(@Nonnull final String x, @Nonnull final Expression<String> y) {
    return new ExpressionImpl.ConcatExpression(literal(x), y);
  }

  /**
   * Create a conjunction (with zero conjuncts).
   * A conjunction with zero conjuncts is true.
   * @return and predicate
   */
  @Override
  public Predicate conjunction() {
    throw new NotImplementedException();
  }

  /**
   * Create a selection item corresponding to a constructor.
   * This method is used to specify a constructor that will be
   * applied to the results of the query execution. If the
   * constructor is for an entity class, the resulting entities
   * will be in the new state after the query is executed.
   * @param resultClass class whose instance is to be constructed
   * @param selections arguments to the constructor
   * @return compound selection item
   * @throws IllegalArgumentException if an argument is a
   * tuple- or array-valued selection item
   */
  @Override
  public <Y> CompoundSelection<Y> construct(@Nonnull final Class<Y> resultClass,
      @Nonnull final Selection<?>... selections) {
    throw new NotImplementedException();
  }

  /**
   * Create an aggregate expression applying the count operation.
   * @param x expression representing input value to count
   * operation
   * @return count expression
   */
  @Override
  public Expression<Long> count(@Nonnull final Expression<?> x) {
    return new ExpressionImpl.AggregationExpression<>(SqlAggregation.COUNT, x);
  }

  /**
   * Create an aggregate expression applying the count distinct
   * operation.
   * @param x expression representing input value to count distinct operation
   * @return count distinct expression
   */
  @Override
  public Expression<Long> countDistinct(@Nonnull final Expression<?> x) {
    return count(new ExpressionImpl.DistinctExpression<>(x));
  }

  @Override
  public <T> CriteriaDelete<T> createCriteriaDelete(final Class<T> targetEntity) {
    throw new NotImplementedException();
  }

  @Override
  public <T> CriteriaUpdate<T> createCriteriaUpdate(final Class<T> targetEntity) {
    throw new NotImplementedException();
  }

  @Override
  public ProcessorCriteriaQuery<Object> createQuery() {
    return new CriteriaQueryImpl<>(Object.class, sd, this);
  }

  @Override
  public <T> ProcessorCriteriaQuery<T> createQuery(final Class<T> resultClass) {
    return new CriteriaQueryImpl<>(resultClass, sd, this);
  }

  @Override
  public ProcessorCriteriaQuery<Tuple> createTupleQuery() {
    return new CriteriaQueryImpl<>(Tuple.class, sd, this);
  }

  @Override
  public Expression<Date> currentDate() {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Time> currentTime() {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Timestamp> currentTimestamp() {
    return new ExpressionImpl.TimeExpression<>(SqlTimeFunctions.TIMESTAMP);
  }

  /**
   * Create an ordering by the descending value of the expression.
   * @param x expression used to define the ordering
   * @return descending ordering corresponding to the expression
   */
  @Override
  public Order desc(@Nonnull final Expression<?> x) {
    return new OrderImpl(false, Objects.requireNonNull((SqlConvertible) x));
  }

  /**
   * Create an expression that returns the difference
   * between its arguments.
   * @param x expression
   * @param y expression
   * @return difference
   */
  @Override
  public <N extends Number> Expression<N> diff(@Nonnull final Expression<? extends N> x,
      @Nonnull final Expression<? extends N> y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, y, SqlArithmetic.DIFF);
  }

  /**
   * Create an expression that returns the difference
   * between its arguments.
   * @param x expression
   * @param y value
   * @return difference
   */
  @Override
  public <N extends Number> Expression<N> diff(@Nonnull final Expression<? extends N> x, @Nonnull final N y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, literal(y), SqlArithmetic.DIFF);
  }

  /**
   * Create an expression that returns the difference
   * between its arguments.
   * @param x value
   * @param y expression
   * @return difference
   */
  @Override
  public <N extends Number> Expression<N> diff(@Nonnull final N x, @Nonnull final Expression<? extends N> y) {
    return new ExpressionImpl.ArithmeticExpression<>(literal(x), y, SqlArithmetic.DIFF);
  }

  /**
   * Create a disjunction (with zero disjuncts).
   * A disjunction with zero disjuncts is false.
   * @return or predicate
   */
  @Override
  public Predicate disjunction() {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate for testing the arguments for equality.
   * @param x expression
   * @param y expression
   * @return equality predicate
   */
  @Override
  public Predicate equal(@Nonnull final Expression<?> x, @Nonnull final Expression<?> y) {// NOSONAR
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.EQ);
  }

  /**
   * Create a predicate for testing the arguments for equality.
   * @param x expression
   * @param y object
   * @return equality predicate
   */
  @Override
  public Predicate equal(@Nonnull final Expression<?> x, final Object y) { // NOSONAR
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.EQ);
  }

  /**
   * Create a predicate testing the existence of a subquery result.
   * @param subquery subquery whose result is to be tested
   * @return exists predicate
   */
  @Override
  public Predicate exists(@Nonnull final Subquery<?> subquery) {
    return new PredicateImpl.SubQuery(subquery, SqlSubQuery.EXISTS);
  }

  /**
   * Create an expression for the execution of a database
   * function.
   * @param name function name
   * @param type expected result type
   * @param args function arguments
   * @return expression
   */
  @SuppressWarnings("unchecked")
  @Override
  public <T> Expression<T> function(@Nonnull final String name, @Nonnull final Class<T> type,
      final Expression<?>... args) {
    final List<Expression<Object>> parameters = args == null ? Collections.emptyList() : Arrays.asList(args).stream()
        .map(
            p -> ((Expression<Object>) p)).collect(Collectors.toList());
    return new ExpressionImpl.FunctionExpression<>(name, type, parameters);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than or equal to the second.
   * @param x expression
   * @param y expression
   * @return greater-than-or-equal predicate
   */
  @Override
  public Predicate ge(@Nonnull final Expression<? extends Number> x, @Nonnull final Expression<? extends Number> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GE);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than or equal to the second.
   * @param x expression
   * @param y expression
   * @return greater-than-or-equal predicate
   */
  @Override
  public Predicate ge(@Nonnull final Expression<? extends Number> x, @Nonnull final Number y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GE);
  }

  public ParameterBuffer getParameter() {
    return parameter;
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than the second.
   * @param x expression
   * @param y expression
   * @return greater-than predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate greaterThan(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Expression<? extends Y> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GT);
  }

  @Override
  public <Y extends Comparable<? super Y>> Predicate greaterThan(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Y y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GT);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than or equal to the second.
   * @param x expression
   * @param y expression
   * @return greater-than-or-equal predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Expression<? extends Y> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GE);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than or equal to the second.
   * @param x expression
   * @param y value
   * @return greater-than-or-equal predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate greaterThanOrEqualTo(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Y y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GE);
  }

  /**
   * Create an aggregate expression for finding the greatest of
   * the values (strings, dates, etc).
   * @param x expression representing input value to greatest
   * operation
   * @return greatest expression
   */
  @Override
  public <X extends Comparable<? super X>> Expression<X> greatest(@Nonnull final Expression<X> x) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than the second.
   * @param x expression
   * @param y expression
   * @return greater-than predicate
   */
  @Override
  public Predicate gt(@Nonnull final Expression<? extends Number> x, @Nonnull final Expression<? extends Number> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GT);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * greater than the second.
   * @param x expression
   * @param y value
   * @return greater-than predicate
   */
  @Override
  public Predicate gt(@Nonnull final Expression<? extends Number> x, @Nonnull final Number y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.GT);
  }

  /**
   * Create predicate to test whether given expression
   * is contained in a list of values.
   * @param expression to be tested against list of values
   * @return in predicate
   */
  @Override
  public <T> In<T> in(final Expression<? extends T> expression) {
    // e.g.: return new Expressions.In<>(expression); //NOSONAR
    throw new NotImplementedException();
  }

  /**
   * Create predicate to test whether given expression
   * is contained in a list of values.
   * @param paths to be tested against list of values
   * @return in predicate
   */
  @Override
  public <T> In<T> in(final List<Path<? extends T>> paths, final Subquery<?> subquery) {
    return new PredicateImpl.In<>(paths, subquery);
  }

  /**
   * Create a predicate that tests whether a collection is empty.<br>
   * Example: "WHERE employee.projects IS EMPTY"
   * @param collection expression
   * @return is-empty predicate
   */
  @Override
  public <C extends Collection<?>> Predicate isEmpty(@Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate testing for a false value.
   * @param x expression to be tested
   * @return predicate
   */
  @Override
  public Predicate isFalse(@Nonnull final Expression<Boolean> x) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate that tests whether an element is
   * a member of a collection (property).
   * If the collection is empty, the predicate will be false.
   * @param elem element expression
   * @param collection expression
   * @return is-member predicate
   */
  @Override
  public <E, C extends Collection<E>> Predicate isMember(@Nonnull final E elem,
      @Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate that tests whether an element is
   * a member of a collection (property).
   * If the collection is empty, the predicate will be false.
   * @param elem element
   * @param collection expression
   * @return is-member predicate
   */
  @Override
  public <E, C extends Collection<E>> Predicate isMember(@Nonnull final Expression<E> elem,
      @Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate that tests whether a collection is not empty.<br>
   * Example: WHERE projects.employees IS NOT EMPTY
   * @param collection expression
   * @return is-not-empty predicate
   */
  @Override
  public <C extends Collection<?>> Predicate isNotEmpty(@Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate that tests whether an element is not a member of a collection.
   * If the collection is empty, the predicate will be true.<br>
   * Example:
   * @param elem element
   * @param collection expression
   * @return is-not-member predicate
   */
  @Override
  public <E, C extends Collection<E>> Predicate isNotMember(@Nonnull final E elem,
      @Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate that tests whether an element is
   * not a member of a collection.
   * If the collection is empty, the predicate will be true.
   * @param elem element expression
   * @param collection expression
   * @return is-not-member predicate
   */
  @Override
  public <E, C extends Collection<E>> Predicate isNotMember(@Nonnull final Expression<E> elem,
      @Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate to test whether the expression is not null.
   * @param x expression
   * @return is-not-null predicate
   */
  @Override
  public Predicate isNotNull(@Nonnull final Expression<?> x) {
    return new PredicateImpl.NullPredicate(x, SqlNullCheck.NOT_NULL);
  }

  /**
   * Create a predicate to test whether the expression is null.
   * @param x expression
   * @return is-null predicate
   */
  @Override
  public Predicate isNull(@Nonnull final Expression<?> x) {
    return new PredicateImpl.NullPredicate(x, SqlNullCheck.NULL);
  }

  /**
   * Create a predicate testing for a true value.
   * @param x expression to be tested
   * @return predicate
   */
  @Override
  public Predicate isTrue(final Expression<Boolean> x) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that returns the keys of a map.
   * @param map map
   * @return set expression
   */
  @Override
  public <K, M extends Map<K, ?>> Expression<Set<K>> keys(@Nonnull final M map) {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate for testing whether the first argument is
   * less than or equal to the second.
   * @param x expression
   * @param y expression
   * @return less-than-or-equal predicate
   */
  @Override
  public Predicate le(@Nonnull final Expression<? extends Number> x, @Nonnull final Expression<? extends Number> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LE);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * less than or equal to the second.
   * @param x expression
   * @param y value
   * @return less-than-or-equal predicate
   */
  @Override
  public Predicate le(@Nonnull final Expression<? extends Number> x, @Nonnull final Number y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LE);
  }

  /**
   * Create an aggregate expression for finding the least of
   * the values (strings, dates, etc).
   * @param x expression representing input value to least
   * operation
   * @return least expression
   */
  @Override
  public <X extends Comparable<? super X>> Expression<X> least(@Nonnull final Expression<X> x) {
    throw new NotImplementedException();
  }

  /**
   * Create expression to return length of a string.
   * @param x string expression
   * @return length expression
   */
  @Override
  public Expression<Integer> length(@Nonnull final Expression<String> x) {
    return new ExpressionImpl.UnaryFunctionalExpression<>(x, SqlStringFunctions.LENGTH);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * less than the second.
   * @param x expression
   * @param y expression
   * @return less-than predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate lessThan(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Expression<? extends Y> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LT);
  }

  @Override
  public <Y extends Comparable<? super Y>> Predicate lessThan(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Y y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LT);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * less than or equal to the second.
   * @param x expression
   * @param y expression
   * @return less-than-or-equal predicate
   */
  @Override
  public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Expression<? extends Y> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LE);
  }

  @Override
  public <Y extends Comparable<? super Y>> Predicate lessThanOrEqualTo(@Nonnull final Expression<? extends Y> x,
      @Nonnull final Y y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LE);
  }

  /**
   * Create a predicate for testing whether the expression
   * satisfies the given pattern.
   * @param x string expression
   * @param pattern string expression
   * @return like predicate
   */
  @Override
  public Predicate like(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern) {
    return new PredicateImpl.LikePredicate(Objects.requireNonNull(x),
        (ParameterExpression<String, ?>) Objects.requireNonNull(pattern));
  }

  /**
   * Create a predicate for testing whether the expression
   * satisfies the given pattern.
   * @param x string expression
   * @param pattern string expression
   * @param escapeChar escape character
   * @return like predicate
   */
  @Override
  public Predicate like(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
      final char escapeChar) {
    return like(x, pattern, literal(escapeChar));
  }

  /**
   * Create a predicate for testing whether the expression
   * satisfies the given pattern.
   * @param x string expression
   * @param pattern string expression
   * @param escapeChar escape character expression
   * @return like predicate
   */
  @Override
  public Predicate like(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
      final Expression<Character> escapeChar) {

    return new PredicateImpl.LikePredicate(Objects.requireNonNull(x),
        (ParameterExpression<String, ?>) Objects.requireNonNull(pattern),
        Optional.ofNullable((ParameterExpression<Character, ?>) (escapeChar)));
  }

  /**
   * Create a predicate for testing whether the expression
   * satisfies the given pattern.
   * @param x string expression
   * @param pattern string
   * @return like predicate
   */
  @Override
  public Predicate like(@Nonnull final Expression<String> x, @Nonnull final String pattern) {
    return like(x, literal(pattern, x));
  }

  /**
   * Create a predicate for testing whether the expression
   * satisfies the given pattern.
   * @param x string expression
   * @param pattern string expression
   * @param escapeChar escape character
   * @return like predicate
   */
  @Override
  public Predicate like(@Nonnull final Expression<String> x, @Nonnull final String pattern,
      final char escapeChar) {
    return like(x, literal(pattern, x), literal(escapeChar));
  }

  @Override
  public Predicate like(final Expression<String> x, final String pattern, final Expression<Character> escapeChar) {
    return like(x, literal(pattern, x), escapeChar);
  }

  /**
   * Create an expression for a literal.
   * @param value value represented by the expression
   * @return expression literal
   * @throws IllegalArgumentException if value is null
   */
  @Override
  public <T> Expression<T> literal(@Nonnull final T value) {
    if (value == null) // NOSONAR
      throw new IllegalArgumentException("Literal value must not be null");
    return parameter.addValue(value);
  }

  private <T> Expression<T> literal(@Nonnull final T value, @Nonnull final Expression<?> x) {
    return parameter.addValue(value, x);
  }

  /**
   * Create expression to locate the position of one string
   * within another, returning position of first character
   * if found.
   * The first position in a string is denoted by 1. If the
   * string to be located is not found, 0 is returned.
   * @param x expression for string to be searched
   * @param pattern expression for string to be located
   * @return expression corresponding to position
   */
  @Override
  public Expression<Integer> locate(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern) {
    return new ExpressionImpl.LocateExpression(x, pattern, null);
  }

  /**
   * Create expression to locate the position of one string
   * within another, returning position of first character
   * if found.
   * The first position in a string is denoted by 1. If the
   * string to be located is not found, 0 is returned.
   * @param x expression for string to be searched
   * @param pattern expression for string to be located
   * @param from expression for position at which to start search
   * @return expression corresponding to position
   */
  @Override
  public Expression<Integer> locate(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
      @Nonnull final Expression<Integer> from) {
    return new ExpressionImpl.LocateExpression(x, pattern, from);
  }

  /**
   * Create expression to locate the position of one string
   * within another, returning position of first character
   * if found.
   * The first position in a string is denoted by 1. If the
   * string to be located is not found, 0 is returned.
   * @param x expression for string to be searched
   * @param pattern string to be located
   * @return expression corresponding to position
   */
  @Override
  public Expression<Integer> locate(@Nonnull final Expression<String> x, @Nonnull final String pattern) {
    return new ExpressionImpl.LocateExpression(x, literal(pattern, x), null);
  }

  /**
   * Create expression to locate the position of one string
   * within another, returning position of first character
   * if found.
   * The first position in a string is denoted by 1. If the
   * string to be located is not found, 0 is returned.
   * @param x expression for string to be searched
   * @param pattern string to be located
   * @param from position at which to start search
   * @return expression corresponding to position
   */
  @Override
  public Expression<Integer> locate(@Nonnull final Expression<String> x, @Nonnull final String pattern,
      final int from) {
    return new ExpressionImpl.LocateExpression(x, literal(pattern), literal(from));
  }

  /**
   * Create expression for converting a string to lowercase.
   * @param x string expression
   * @return expression to convert to lowercase
   */
  @Override
  public Expression<String> lower(@Nonnull final Expression<String> x) {
    return new ExpressionImpl.UnaryFunctionalExpression<>(x, SqlStringFunctions.LOWER);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * less than the second.
   * @param x expression
   * @param y expression
   * @return less-than predicate
   */
  @Override
  public Predicate lt(@Nonnull final Expression<? extends Number> x, @Nonnull final Expression<? extends Number> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LT);
  }

  /**
   * Create a predicate for testing whether the first argument is
   * less than the second.
   * @param x expression
   * @param y value
   * @return less-than predicate
   */
  @Override
  public Predicate lt(@Nonnull final Expression<? extends Number> x, @Nonnull final Number y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.LT);
  }

  /**
   * Create an aggregate expression applying the numerical max
   * operation.
   * @param x expression representing input value to max operation
   * @return max expression
   */
  @Override
  public <N extends Number> Expression<N> max(@Nonnull final Expression<N> x) {
    throw new NotImplementedException();
  }

  /**
   * Create an aggregate expression applying the numerical min
   * operation.
   * @param x expression representing input value to min operation
   * @return min expression
   */
  @Override
  public <N extends Number> Expression<N> min(@Nonnull final Expression<N> x) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that returns the modulus
   * of its arguments.
   * @param x expression
   * @param y expression
   * @return modulus
   */
  @Override
  public Expression<Integer> mod(@Nonnull final Expression<Integer> x, @Nonnull final Expression<Integer> y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, y, SqlArithmetic.MOD);
  }

  /**
   * Create an expression that returns the modulus
   * of its arguments.
   * @param x expression
   * @param y value
   * @return modulus
   */
  @Override
  public Expression<Integer> mod(@Nonnull final Expression<Integer> x, @Nonnull final Integer y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, literal(y), SqlArithmetic.MOD);
  }

  /**
   * Create an expression that returns the modulus
   * of its arguments.
   * @param x value
   * @param y expression
   * @return modulus
   */
  @Override
  public Expression<Integer> mod(@Nonnull final Integer x, @Nonnull final Expression<Integer> y) {
    return new ExpressionImpl.ArithmeticExpression<>(literal(x), y, SqlArithmetic.MOD);
  }

  /**
   * Create an expression that returns the arithmetic negation
   * of its argument.
   * @param x expression
   * @return arithmetic negation
   */
  @Override
  public <N extends Number> Expression<N> neg(@Nonnull final Expression<N> x) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate not(@Nonnull final Expression<Boolean> restriction) {
    return new PredicateImpl.NotPredicate((SqlConvertible) restriction);
  }

  /**
   * Create a predicate for testing the arguments for inequality.
   * @param x expression
   * @param y expression
   * @return inequality predicate
   */
  @Override
  public Predicate notEqual(@Nonnull final Expression<?> x, @Nonnull final Expression<?> y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.NE);
  }

  /**
   * Create a predicate for testing the arguments for inequality.
   * @param x expression
   * @param y object
   * @return inequality predicate
   */
  @Override
  public Predicate notEqual(@Nonnull final Expression<?> x, @Nonnull final Object y) {
    return binaryExpression(x, y, PredicateImpl.BinaryExpressionPredicate.Operation.NE);
  }

  /**
   * Create a predicate for testing whether the expression
   * does not satisfy the given pattern.
   * @param x string expression
   * @param pattern string expression
   * @return not-like predicate
   */
  @Override
  public Predicate notLike(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern) {
    return not(like(x, pattern));
  }

  /**
   * Create a predicate for testing whether the expression
   * satisfies the given pattern.
   * @param x string expression
   * @param pattern string
   * @param escapeChar escape character
   * @return like predicate
   */
  @Override
  public Predicate notLike(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
      final char escapeChar) {
    return not(like(x, pattern, escapeChar));
  }

  /**
   * Create a predicate for testing whether the expression
   * does not satisfy the given pattern.
   * @param x string expression
   * @param pattern string expression
   * @param escapeChar escape character expression
   * @return not-like predicate
   */
  @Override
  public Predicate notLike(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
      @Nonnull final Expression<Character> escapeChar) {
    return not(like(x, pattern, escapeChar));
  }

  /**
   * Create a predicate for testing whether the expression
   * does not satisfy the given pattern.
   * @param x string expression
   * @param pattern string
   * @return not-like predicate
   */
  @Override
  public Predicate notLike(@Nonnull final Expression<String> x, @Nonnull final String pattern) {
    return not(like(x, pattern));
  }

  /**
   * Create a predicate for testing whether the expression
   * does not satisfy the given pattern.
   * @param x string expression
   * @param pattern string
   * @param escapeChar escape character
   * @return not-like predicate
   */
  @Override
  public Predicate notLike(@Nonnull final Expression<String> x, @Nonnull final String pattern,
      final char escapeChar) {
    return not(like(x, pattern, escapeChar));
  }

  /**
   * Create a predicate for testing whether the expression
   * does not satisfy the given pattern.
   * @param x string expression
   * @param pattern string
   * @param escapeChar escape character expression
   * @return not-like predicate
   */
  @Override
  public Predicate notLike(@Nonnull final Expression<String> x, @Nonnull final String pattern,
      @Nonnull final Expression<Character> escapeChar) {
    return not(like(x, pattern, escapeChar));
  }

  /**
   * Create an expression that tests whether its argument are
   * equal, returning null if they are and the value of the
   * first expression if they are not.
   * @param x expression
   * @param y expression
   * @return nullif expression
   */
  @Override
  public <Y> Expression<Y> nullif(@Nonnull final Expression<Y> x, @Nonnull final Expression<?> y) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that tests whether its argument are
   * equal, returning null if they are and the value of the
   * first expression if they are not.
   * @param x expression
   * @param y value
   * @return nullif expression
   */
  @Override
  public <Y> Expression<Y> nullif(@Nonnull final Expression<Y> x, @Nonnull final Y y) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression for a null literal with the given type.
   * @param resultClass type of the null literal
   * @return null expression literal
   */
  @Override
  public <T> Expression<T> nullLiteral(@Nonnull final Class<T> resultClass) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate or(@Nonnull final Expression<Boolean> x, @Nonnull final Expression<Boolean> y) {
    return new PredicateImpl.OrPredicate(x, y);
  }

  @Override
  public Predicate or(final Predicate... restrictions) {
    return PredicateImpl.or(restrictions);
  }

  /**
   * Create a parameter expression.
   * @param paramClass parameter class
   * @return parameter expression
   */
  @Override
  public <T> javax.persistence.criteria.ParameterExpression<T> parameter(@Nonnull final Class<T> paramClass) {
    throw new NotImplementedException();
  }

  /**
   * Create a parameter expression with the given name.
   * @param paramClass parameter class
   * @param name name that can be used to refer to
   * the parameter
   * @return parameter expression
   */
  @Override
  public <T> javax.persistence.criteria.ParameterExpression<T> parameter(@Nonnull final Class<T> paramClass,
      @Nonnull final String name) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that returns the product
   * of its arguments.
   * @param x expression
   * @param y expression
   * @return product
   */
  @Override
  public <N extends Number> Expression<N> prod(@Nonnull final Expression<? extends N> x,
      @Nonnull final Expression<? extends N> y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, y, SqlArithmetic.PROD);
  }

  /**
   * Create an expression that returns the product
   * of its arguments.
   * @param x expression
   * @param y value
   * @return product
   */
  @Override
  public <N extends Number> Expression<N> prod(@Nonnull final Expression<? extends N> x, @Nonnull final N y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, literal(y), SqlArithmetic.PROD);
  }

  /**
   * Create an expression that returns the product
   * of its arguments.
   * @param x value
   * @param y expression
   * @return product
   */
  @Override
  public <N extends Number> Expression<N> prod(@Nonnull final N x, @Nonnull final Expression<? extends N> y) {
    return new ExpressionImpl.ArithmeticExpression<>(literal(x), y, SqlArithmetic.PROD);
  }

  /**
   * Create an expression that returns the quotient
   * of its arguments.
   * @param x expression
   * @param y expression
   * @return quotient
   */
  @Override
  public Expression<Number> quot(@Nonnull final Expression<? extends Number> x,
      @Nonnull final Expression<? extends Number> y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, y, SqlArithmetic.QUOT);
  }

  /**
   * Create an expression that returns the quotient
   * of its arguments.
   * @param x expression
   * @param y value
   * @return quotient
   */
  @Override
  public Expression<Number> quot(@Nonnull final Expression<? extends Number> x, @Nonnull final Number y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, literal(y), SqlArithmetic.QUOT);
  }

  /**
   * Create an expression that returns the quotient
   * of its arguments.
   * @param x value
   * @param y expression
   * @return quotient
   */
  @Override
  public Expression<Number> quot(@Nonnull final Number x, @Nonnull final Expression<? extends Number> y) {
    return new ExpressionImpl.ArithmeticExpression<>(literal(x), y, SqlArithmetic.QUOT);
  }

  /**
   * Create a general case expression.
   * @return general case expression
   */
  @Override
  public <R> Case<R> selectCase() {
    throw new NotImplementedException();
  }

  /**
   * Create a simple case expression.
   * @param expression to be tested against the case conditions
   * @return simple case expression
   */
  @Override
  public <C, R> SimpleCase<C, R> selectCase(final Expression<? extends C> expression) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that tests the size of a collection.
   * @param collection collection
   * @return size expression
   */
  @Override
  public <C extends Collection<?>> Expression<Integer> size(@Nonnull final C collection) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that tests the size of a collection.
   * @param collection expression
   * @return size expression
   */
  @Override
  public <C extends Collection<?>> Expression<Integer> size(@Nonnull final Expression<C> collection) {
    throw new NotImplementedException();
  }

  /**
   * Create a some expression over the subquery results.
   * This expression is equivalent to an <code>any</code> expression.
   * @param subquery subquery
   * @return some expression
   */
  @Override
  public <Y> Expression<Y> some(@Nonnull final Subquery<Y> subquery) {
    return new ExpressionImpl.SubQuery<>(subquery, SqlSubQuery.SOME);
  }

  @Override
  public Expression<Double> sqrt(final Expression<? extends Number> x) {
    // "SQRT"
    return null;
  }

  /**
   * Create an expression for substring extraction.
   * Extracts a substring starting at the specified position
   * through to end of the string.
   * First position is 1.
   * @param x string expression
   * @param from start position expression
   * @return expression corresponding to substring extraction
   */
  @Override
  public Expression<String> substring(@Nonnull final Expression<String> x, @Nonnull final Expression<Integer> from) {
    return new ExpressionImpl.SubstringExpression(x, from, null);
  }

  /**
   * Create an expression for substring extraction.
   * Extracts a substring of given length starting at the
   * specified position.
   * First position is 1.
   * @param x string expression
   * @param from start position expression
   * @param len length expression
   * @return expression corresponding to substring extraction
   */
  @Override
  public Expression<String> substring(@Nonnull final Expression<String> x, @Nonnull final Expression<Integer> from,
      @Nonnull final Expression<Integer> len) {
    return new ExpressionImpl.SubstringExpression(x, from, len);
  }

  /**
   * Create an expression for substring extraction.
   * Extracts a substring starting at the specified position
   * through to end of the string.
   * First position is 1.
   * @param x string expression
   * @param from start position
   * @return expression corresponding to substring extraction
   */
  @Override
  public Expression<String> substring(@Nonnull final Expression<String> x, @Nonnull final int from) {
    return new ExpressionImpl.SubstringExpression(x, literal(from), null);
  }

  /**
   * Create an expression for substring extraction.
   * Extracts a substring of given length starting at the
   * specified position.
   * First position is 1.
   * @param x string expression
   * @param from start position
   * @param len length
   * @return expression corresponding to substring extraction
   */
  @Override
  public Expression<String> substring(final Expression<String> x, final int from, final int len) {
    return new ExpressionImpl.SubstringExpression(x, literal(from), literal(len));
  }

  /**
   * Create an expression that returns the sum
   * of its arguments.
   * @param x expression
   * @param y expression
   * @return sum
   */
  @Override
  public <N extends Number> Expression<N> sum(@Nonnull final Expression<? extends N> x,
      @Nonnull final Expression<? extends N> y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, y, SqlArithmetic.SUM);
  }

  /**
   * Create an expression that returns the sum
   * of its arguments.
   * @param x expression
   * @param y value
   * @return sum
   */
  @Override
  public <N extends Number> Expression<N> sum(@Nonnull final Expression<? extends N> x, @Nonnull final N y) {
    return new ExpressionImpl.ArithmeticExpression<>(x, literal(y, x), SqlArithmetic.SUM);
  }

  /**
   * Create an aggregate expression applying the sum operation.
   * @param x expression representing input value to sum operation
   * @return sum expression
   */
  @Override
  public <N extends Number> Expression<N> sum(@Nonnull final Expression<N> x) {
    throw new NotImplementedException();
  }

  /**
   * Create an expression that returns the sum
   * of its arguments.
   * @param x value
   * @param y expression
   * @return sum
   */
  @Override
  public <N extends Number> Expression<N> sum(@Nonnull final N x, @Nonnull final Expression<? extends N> y) {
    return new ExpressionImpl.ArithmeticExpression<>(literal(x), y, SqlArithmetic.SUM);
  }

  /**
   * Create an aggregate expression applying the sum operation to a
   * Float-valued expression, returning a Double result.
   * @param x expression representing input value to sum operation
   * @return sum expression
   */
  @Override
  public Expression<Double> sumAsDouble(@Nonnull final Expression<Float> x) {
    throw new NotImplementedException();
  }

  /**
   * Create an aggregate expression applying the sum operation to an
   * Integer-valued expression, returning a Long result.
   * @param x expression representing input value to sum operation
   * @return sum expression
   */
  @Override
  public Expression<Long> sumAsLong(@Nonnull final Expression<Integer> x) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<BigDecimal> toBigDecimal(@Nonnull final Expression<? extends Number> number) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<BigInteger> toBigInteger(@Nonnull final Expression<? extends Number> number) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Double> toDouble(@Nonnull final Expression<? extends Number> number) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Float> toFloat(@Nonnull final Expression<? extends Number> number) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Integer> toInteger(@Nonnull final Expression<? extends Number> number) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<Long> toLong(@Nonnull final Expression<? extends Number> number) {
    throw new NotImplementedException();
  }

  @Override
  public Expression<String> toString(@Nonnull final Expression<Character> character) {
    throw new NotImplementedException();
  }

  /**
   * Downcast CollectionJoin object to the specified type.
   * @param join CollectionJoin object
   * @param type type to be downcast to
   * @return CollectionJoin object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, T, E extends T> CollectionJoin<X, E> treat(@Nonnull final CollectionJoin<X, T> join,
      @Nonnull final Class<E> type) {
    throw new NotImplementedException();
  }

  /**
   * Downcast Join object to the specified type.
   * @param join Join object
   * @param type type to be downcast to
   * @return Join object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, T, V extends T> Join<X, V> treat(@Nonnull final Join<X, T> join, @Nonnull final Class<V> type) {
    throw new NotImplementedException();
  }

  /**
   * Downcast ListJoin object to the specified type.
   * @param join ListJoin object
   * @param type type to be downcast to
   * @return ListJoin object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, T, E extends T> ListJoin<X, E> treat(@Nonnull final ListJoin<X, T> join, @Nonnull final Class<E> type) {
    throw new NotImplementedException();
  }

  /**
   * Downcast MapJoin object to the specified type.
   * @param join MapJoin object
   * @param type type to be downcast to
   * @return MapJoin object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, K, T, V extends T> MapJoin<X, K, V> treat(@Nonnull final MapJoin<X, K, T> join,
      @Nonnull final Class<V> type) {
    throw new NotImplementedException();
  }

  /**
   * Downcast Path object to the specified type.
   * @param path path
   * @param type type to be downcast to
   * @return Path object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, T extends X> Path<T> treat(@Nonnull final Path<X> path, @Nonnull final Class<T> type) {
    throw new NotImplementedException();
  }

  /**
   * Downcast Root object to the specified type.
   * @param root root
   * @param type type to be downcast to
   * @return Root object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, T extends X> Root<T> treat(@Nonnull final Root<X> root, @Nonnull final Class<T> type) {
    throw new NotImplementedException();
  }

  /**
   * Downcast SetJoin object to the specified type.
   * @param join SetJoin object
   * @param type type to be downcast to
   * @return SetJoin object of the specified type
   * @since Java Persistence 2.1
   */
  @Override
  public <X, T, E extends T> SetJoin<X, E> treat(@Nonnull final SetJoin<X, T> join, @Nonnull final Class<E> type) {
    throw new NotImplementedException();
  }

  /**
   * Create expression to trim character from both ends of
   * a string.
   * @param t character to be trimmed
   * @param x expression for string to trim
   * @return trim expression
   */
  @Override
  public Expression<String> trim(final char t, @Nonnull final Expression<String> x) {
    throw new NotImplementedException();
  }

  /**
   * Create expression to trim character from both ends of
   * a string.
   * @param t expression for character to be trimmed
   * @param x expression for string to trim
   * @return trim expression
   */
  @Override
  public Expression<String> trim(@Nonnull final Expression<Character> t, @Nonnull final Expression<String> x) {
    throw new NotImplementedException();
  }

  /**
   * Create expression to trim blanks from both ends of
   * a string.
   * @param x expression for string to trim
   * @return trim expression
   */
  @Override
  public Expression<String> trim(@Nonnull final Expression<String> x) {
    return new ExpressionImpl.UnaryFunctionalExpression<>(x, SqlStringFunctions.TRIM);
  }

  /**
   * Create expression to trim character from a string.
   * @param ts trim specification
   * @param t expression for character to be trimmed
   * @param x expression for string to trim
   * @return trim expression
   */
  @Override
  public Expression<String> trim(@Nonnull final Trimspec ts, final char t, @Nonnull final Expression<String> x) {
    throw new NotImplementedException();
  }

  /**
   * Create expression to trim character from a string.
   * @param ts trim specification
   * @param t expression for character to be trimmed
   * @param x expression for string to trim
   * @return trim expression
   */
  @Override
  public Expression<String> trim(@Nonnull final Trimspec ts, @Nonnull final Expression<Character> t,
      @Nonnull final Expression<String> x) {
    throw new NotImplementedException();
  }

  /**
   * Create expression to trim blanks from a string.
   * @param ts trim specification
   * @param x expression for string to trim
   * @return trim expression
   */
  @Override
  public Expression<String> trim(@Nonnull final Trimspec ts, @Nonnull final Expression<String> x) {
    throw new NotImplementedException();
  }

  /**
   * Create a tuple-valued selection item.
   * @param selections selection items
   * @return tuple-valued compound selection
   * @throws IllegalArgumentException if an argument is a
   * tuple- or array-valued selection item
   */
  @Override
  public CompoundSelection<Tuple> tuple(@Nonnull final Selection<?>... selections) {
    throw new NotImplementedException();
  }

  /**
   * Create expression for converting a string to uppercase.
   * @param x string expression
   * @return expression to convert to uppercase
   */
  @Override
  public Expression<String> upper(@Nonnull final Expression<String> x) {
    return new ExpressionImpl.UnaryFunctionalExpression<>(x, SqlStringFunctions.UPPER);
  }

  @Override
  public <V, M extends Map<?, V>> Expression<Collection<V>> values(@Nonnull final M map) {
    throw new NotImplementedException();
  }

  /**
   * Creates an expression for a row number function.
   */
  @Override
  public WindowFunction<Long> rowNumber() {
    return new ExpressionImpl.WindowFunctionExpression<>(SqlWindowFunctions.ROW_NUMBER);
  }

  public JPAServiceDocument getServiceDocument() {
    return sd;
  }

  private Predicate binaryExpression(@Nonnull final Expression<?> x, @Nonnull final Expression<?> y,
      @Nonnull final Operation p) {

    if (Objects.requireNonNull(y) instanceof ParameterExpression)
      ((ParameterExpression<?, ?>) y).setPath(x);
    if (Objects.requireNonNull(x) instanceof ParameterExpression)
      ((ParameterExpression<?, ?>) x).setPath(y);
    return new PredicateImpl.BinaryExpressionPredicate(p, x, y);
  }

  private Predicate binaryExpression(@Nonnull final Expression<?> x, @Nonnull final Object y,
      @Nonnull final Operation p) {
    return new PredicateImpl.BinaryExpressionPredicate(p, Objects.requireNonNull(x), literal(y, x));
  }
}
