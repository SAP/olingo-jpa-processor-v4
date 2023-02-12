package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Parameter;
import javax.persistence.criteria.CriteriaBuilder.Coalesce;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.Bindable;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SingularAttribute;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.ProcessorCriteriaBuilder.WindowFunction;
import com.sap.olingo.jpa.processor.cb.exceptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

abstract class ExpressionImpl<T> implements Expression<T>, SqlConvertible {

  public static final String OPENING_BRACKET = "(";
  public static final String CLOSING_BRACKET = ")";
  public static final String DOT = ".";
  public static final String SELECTION_REPLACEMENT = "_";
  public static final String SELECTION_REPLACEMENT_REGEX = "\\.|/";
  protected Optional<String> alias = Optional.empty();

  /**
   * Assigns an alias to the selection item.
   * Once assigned, an alias cannot be changed or reassigned.
   * Returns the same selection item.
   * @param name alias
   * @return selection item
   */
  @Override
  public Selection<T> alias(final String name) {
    if (alias.isPresent())
      throw new IllegalAccessError("Alias can only be set once");
    alias = Optional.of(name);
    return this;
  }

  /**
   * Perform a typecast upon the expression, returning a new
   * expression object.
   * This method does not cause type conversion:
   * the runtime type is not changed.
   * Warning: may result in a runtime failure.
   * @param type intended type of the expression
   * @return new expression of the given type
   */
  @Override
  public <X> Expression<X> as(final Class<X> type) {
    throw new NotImplementedException();
  }

  @Override
  public String getAlias() {
    return alias.orElse("");
  }

  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    throw new NotImplementedException();
  }

  @Override
  public Class<? extends T> getJavaType() {
    return null;
  }

  @Override
  public Predicate in(final Collection<?> values) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(final Expression<?>... values) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(final Expression<Collection<?>> values) {
    throw new NotImplementedException();
  }

  @Override
  public Predicate in(final Object... values) {
    throw new NotImplementedException();
  }

  @Override
  public boolean isCompoundSelection() {
    return false;
  }

  /**
   * Create a predicate to test whether the expression is not null.
   * @return predicate testing whether the expression is not null
   */
  @Override
  public Predicate isNotNull() {
    throw new NotImplementedException();
  }

  /**
   * Create a predicate to test whether the expression is null.
   * @return predicate testing whether the expression is null
   */
  @Override
  public Predicate isNull() {
    throw new NotImplementedException();
  }

  static class AggregationExpression<N extends Number> extends ExpressionImpl<N> {

    private final SqlAggregation function;
    private final SqlConvertible expression;

    AggregationExpression(@Nonnull final SqlAggregation function, @Nonnull final Expression<?> x) {
      this.function = Objects.requireNonNull(function);
      this.expression = Objects.requireNonNull((SqlConvertible) x);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(function)
          .append(OPENING_BRACKET);
      return expression(statement)
          .append(CLOSING_BRACKET);
    }

    private StringBuilder expression(final StringBuilder statement) {
      if (expression instanceof FromImpl<?, ?>) {
        final FromImpl<?, ?> from = (FromImpl<?, ?>) expression;
        final String tableAlias = from.tableAlias.orElseThrow(IllegalStateException::new);
        try {
          final List<JPAAttribute> keys = from.st.getKey();
          statement
              .append(tableAlias)
              .append(DOT)
              .append(from.st.getPath(keys.get(0).getExternalName()).getDBFieldName());
        } catch (final ODataJPAModelException e) {
          throw new IllegalArgumentException(e);
        }
        return statement;
      } else {
        return expression.asSQL(statement);
      }
    }

    SqlConvertible getExpression() {
      return expression;
    }
  }

  static class ArithmeticExpression<N extends Number> extends ExpressionImpl<N> {

    private final SqlConvertible left;
    private final SqlConvertible right;
    private final SqlArithmetic operation;

    ArithmeticExpression(@Nonnull final Expression<? extends N> x, @Nonnull final Expression<? extends N> y,
        @Nonnull final SqlArithmetic operation) {

      this.left = (SqlConvertible) Objects.requireNonNull(x);
      this.right = (SqlConvertible) Objects.requireNonNull(y);
      this.operation = Objects.requireNonNull(operation);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      left.asSQL(statement.append(OPENING_BRACKET))
          .append(" ")
          .append(operation)
          .append(" ");
      return right.asSQL(statement)
          .append(CLOSING_BRACKET);
    }

  }

  static class CoalesceExpression<T> extends ExpressionImpl<T> implements Coalesce<T> {
    private final List<Expression<T>> values;

    CoalesceExpression() {
      super();
      this.values = new ArrayList<>();
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {

      statement.append(SqlKeyWords.COALESCE)
          .append(OPENING_BRACKET);
      statement.append(values.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, ", ")));
      return statement.append(CLOSING_BRACKET);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Coalesce<T> value(@Nonnull final Expression<? extends T> value) {
      values.add((Expression<T>) value);
      return this;
    }

    @Override
    public Coalesce<T> value(@Nonnull final T value) {
      throw new NotImplementedException();
    }
  }

  static class ConcatExpression extends ExpressionImpl<String> {
    private final SqlConvertible first;
    private final SqlConvertible second;

    ConcatExpression(@Nonnull final Expression<String> first, @Nonnull final Expression<String> second) {
      this.first = (SqlConvertible) Objects.requireNonNull(first);
      this.second = (SqlConvertible) Objects.requireNonNull(second);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(SqlStringFunctions.CONCAT)
          .append(OPENING_BRACKET);
      first.asSQL(statement)
          .append(", ");
      second.asSQL(statement);
      return statement.append(CLOSING_BRACKET);
    }
  }

  static class DistinctExpression<T> extends ExpressionImpl<T> {
    private final SqlConvertible left;

    DistinctExpression(@Nonnull final Expression<?> x) {
      this.left = (SqlConvertible) Objects.requireNonNull(x);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {

      if (left instanceof FromImpl<?, ?>) {
        try {
          final FromImpl<?, ?> from = ((FromImpl<?, ?>) left);
          statement.append(SqlKeyWords.DISTINCT)
              .append(OPENING_BRACKET);
          statement.append(from.st.getKey().stream()
              .map(a -> from.get(a.getInternalName()))
              .collect(new StringBuilderCollector.ExpressionCollector(statement, ", ")));
          return statement.append(CLOSING_BRACKET);
        } catch (final ODataJPAModelException e) {
          throw new IllegalStateException(e);
        }

      }
      return left.asSQL(statement.append(SqlKeyWords.DISTINCT).append(OPENING_BRACKET)).append(CLOSING_BRACKET);
    }
  }

  static class FunctionExpression<T> extends ExpressionImpl<T> {

    private final String functionName;
    private final Class<T> type;
    private final List<Expression<Object>> args;

    FunctionExpression(@Nonnull final String name, @Nonnull final Class<T> type, final List<Expression<Object>> args) {
      this.functionName = Objects.requireNonNull(name);
      this.type = Objects.requireNonNull(type);
      this.args = Objects.requireNonNull(args);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(functionName).append(OPENING_BRACKET);
      statement.append(args.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, ", ")));
      return statement.append(CLOSING_BRACKET);
    }

    @Override
    public Class<? extends T> getJavaType() {
      return type;
    }

  }

  static class LocateExpression extends ExpressionImpl<Integer> {
    private final SqlConvertible expression;
    private final SqlConvertible pattern;
    private final Optional<SqlConvertible> from;

    LocateExpression(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
        final Expression<Integer> from) {
      this.expression = (SqlConvertible) Objects.requireNonNull(x);
      this.pattern = (SqlConvertible) Objects.requireNonNull(pattern);
      this.from = Optional.ofNullable((SqlConvertible) from);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      // LOCATE(<pattern>, <string>, <from>)
      statement.append(SqlStringFunctions.LOCATE)
          .append(OPENING_BRACKET);
      pattern.asSQL(statement)
          .append(", ");
      expression.asSQL(statement);
      from.ifPresent(l -> l.asSQL(statement.append(", ")));
      return statement.append(CLOSING_BRACKET);
    }
  }

  static final class ParameterExpression<T, S> extends ExpressionImpl<T> implements Parameter<T> {

    private final Integer index;
    private final S value;
    private Optional<AttributeConverter<S, T>> converter;
    private Optional<JPAPath> jpaPath;

    ParameterExpression(final Integer i, final S value) {
      this.index = i;
      this.value = value;
      this.converter = Optional.empty();
      this.jpaPath = Optional.empty();
    }

    ParameterExpression(final Integer i, final S value, @Nullable final Expression<?> x) {
      this.index = i;
      this.value = value;
      setPath(x);
    }

    @SuppressWarnings("unchecked")
    T getValue() {
      if (converter.isPresent())
        return converter.get().convertToDatabaseColumn(value);
      if (jpaPath.isPresent() && jpaPath.get().getLeaf().isEnum())
        return (T) ((Number) ((Enum<?>) value).ordinal());
      return (T) value;
    }

    void setPath(@Nullable final Expression<?> x) {
      if (x instanceof PathImpl && ((PathImpl<?>) x).path.isPresent()) {
        jpaPath = Optional.of(((PathImpl<?>) x).path.get()); // NOSONAR
        converter = Optional.ofNullable(jpaPath.get().getLeaf().getConverter());
      } else {
        this.converter = Optional.empty();
        this.jpaPath = Optional.empty();
      }
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      return statement.append("?").append(index.toString());
    }

    @Override
    public String getName() {
      return null;
    }

    @Override
    public Integer getPosition() {
      return index;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<T> getParameterType() {
      return (Class<T>) value.getClass();
    }

    @Override
    public Class<? extends T> getJavaType() {
      return getParameterType();
    }

    @Override
    public int hashCode() {
      return Objects.hash(jpaPath, value);
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) return true;
      if (!(obj instanceof ParameterExpression)) return false;
      final ParameterExpression<?, ?> other = (ParameterExpression<?, ?>) obj;
      return Objects.equals(jpaPath, other.jpaPath) && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
      return "ParameterExpression [jpaPath=" + jpaPath + ", value=" + value + ", index=" + index + "]";
    }
  }

  static final class SubQuery<X> extends ExpressionImpl<X> {

    private final SqlConvertible query;
    private final SqlSubQuery operator;

    SubQuery(@Nonnull final Subquery<?> subquery, @Nonnull final SqlSubQuery operator) {
      this.query = (SqlConvertible) Objects.requireNonNull(subquery);
      this.operator = operator;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(operator)
          .append(" ")
          .append(OPENING_BRACKET);
      return query.asSQL(statement).append(CLOSING_BRACKET);
    }

  }

  static class SubstringExpression extends ExpressionImpl<String> {
    private final SqlConvertible expression;
    private final SqlConvertible from;
    private final Optional<SqlConvertible> len;

    SubstringExpression(@Nonnull final Expression<String> x, @Nonnull final Expression<Integer> from,
        final Expression<Integer> len) {
      this.expression = (SqlConvertible) Objects.requireNonNull(x);
      this.from = (SqlConvertible) Objects.requireNonNull(from);
      this.len = Optional.ofNullable((SqlConvertible) len);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(SqlStringFunctions.SUBSTRING)
          .append(OPENING_BRACKET);
      expression.asSQL(statement)
          .append(", ");
      from.asSQL(statement);
      len.ifPresent(l -> l.asSQL(statement.append(", ")));
      return statement.append(CLOSING_BRACKET);
    }
  }

  static class TimeExpression<T> extends ExpressionImpl<T> {

    private final SqlTimeFunctions function;

    TimeExpression(@Nonnull final SqlTimeFunctions function) {
      this.function = Objects.requireNonNull(function);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      return statement.append(function);
    }

  }

  static class UnaryFunctionalExpression<T> extends ExpressionImpl<T> {
    private final SqlConvertible left;
    private final SqlStringFunctions function;

    UnaryFunctionalExpression(@Nonnull final Expression<?> x, @Nonnull final SqlStringFunctions function) {
      this.left = (SqlConvertible) Objects.requireNonNull(x);
      this.function = Objects.requireNonNull(function);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(function).append(OPENING_BRACKET);
      return left.asSQL(statement).append(CLOSING_BRACKET);
    }

  }

  static class WindowFunctionExpression<T> extends ExpressionImpl<T> implements WindowFunction<T> {
    private final SqlWindowFunctions function;
    private Optional<List<Order>> orderBy;
    private Optional<List<Path<?>>> partitionBy;

    WindowFunctionExpression(@Nonnull final SqlWindowFunctions function) {
      this.function = function;
      this.orderBy = Optional.empty();
      this.partitionBy = Optional.empty();
    }
    // https://www.h2database.com/html/functions-window.html

    // window_function_name ( expression ) OVER (
    // partition_clause
    // order_clause
    // frame_clause
    // )

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(function)
          .append(OPENING_BRACKET)
          .append(CLOSING_BRACKET)
          .append(" OVER")
          .append(OPENING_BRACKET);
      partitionBy.ifPresent(p -> {
        statement
            .append(" ")
            .append(SqlKeyWords.PARTITION)
            .append(" ");
        statement.append(p.stream().collect(new StringBuilderCollector.ExpressionCollector(statement, ", ")));
      });
      orderBy.ifPresent(o -> {
        statement
            .append(" ")
            .append(SqlKeyWords.ORDERBY)
            .append(" ");
        statement.append(o.stream().collect(new StringBuilderCollector.OrderCollector(statement, ", ")));
      });
      return statement.append(CLOSING_BRACKET);
    }

    @Override
    public WindowFunction<T> orderBy(final Order... o) {
      this.orderBy = Optional.ofNullable(Arrays.asList(o));
      return this;
    }

    @Override
    public WindowFunction<T> orderBy(final List<Order> o) {
      this.orderBy = Optional.ofNullable(o);
      return this;
    }

    @Override
    public WindowFunction<T> partitionBy(final Path<?>... p) {
      this.partitionBy = Optional.ofNullable(Arrays.asList(p));
      return this;
    }

    @Override
    public WindowFunction<T> partitionBy(final List<Path<?>> p) {
      this.partitionBy = Optional.ofNullable(p);
      return this;
    }

    @Override
    public Path<T> asPath(final String tableAlias) {
      return new ExpressionPath<>(alias, tableAlias);
    }
  }

  static class ExpressionPath<T> extends ExpressionImpl<T> implements Path<T> {

    private final Optional<String> dbFieldName;
    private final Optional<String> tableAlias;

    ExpressionPath(final Optional<String> dbFieldName, final String tableAlias) {
      this.dbFieldName = dbFieldName;
      this.tableAlias = Optional.of(tableAlias);
    }

    ExpressionPath(final String dbFieldName, final Optional<String> tableAlias) {
      this.dbFieldName = Optional.of(dbFieldName);
      this.tableAlias = tableAlias;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      tableAlias.ifPresent(p -> {
        statement.append(p);
        statement.append(DOT);
      });
      statement.append(dbFieldName.orElseThrow(() -> new IllegalStateException("Missing name")));
      return statement;
    }

    @Override
    public Bindable<T> getModel() {
      throw new NotImplementedException();
    }

    @Override
    public Path<?> getParentPath() {
      throw new NotImplementedException();
    }

    @Override
    public <Y> Path<Y> get(final SingularAttribute<? super T, Y> attribute) {
      throw new NotImplementedException();
    }

    @Override
    public <E, C extends Collection<E>> Expression<C> get(final PluralAttribute<T, C, E> collection) {
      throw new NotImplementedException();
    }

    @Override
    public <K, V, M extends Map<K, V>> Expression<M> get(final MapAttribute<T, K, V> map) {
      throw new NotImplementedException();
    }

    @Override
    public Expression<Class<? extends T>> type() {
      throw new NotImplementedException();
    }

    @Override
    public <Y> Path<Y> get(final String attributeName) {
      throw new NotImplementedException();
    }
  }
}
