package com.sap.olingo.jpa.processor.cb.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.persistence.AttributeConverter;
import javax.persistence.Parameter;
import javax.persistence.criteria.CriteriaBuilder.Coalesce;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAttribute;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.cb.api.SqlAggregation;
import com.sap.olingo.jpa.processor.cb.api.SqlArithmetic;
import com.sap.olingo.jpa.processor.cb.api.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;
import com.sap.olingo.jpa.processor.cb.api.SqlStringFunctions;
import com.sap.olingo.jpa.processor.cb.api.SqlSubQuery;
import com.sap.olingo.jpa.processor.cb.api.SqlTimeFunctions;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

abstract class ExpressionImpl<T> implements Expression<T>, SqlConvertible {

  public static final String OPENING_BRACKET = "(";
  public static final String CLOSING_BRACKET = ")";
  public static final String DOT = ".";

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
  public <X> Expression<X> as(Class<X> type) {
    throw new NotImplementedException();
  }

  @Override
  public String getAlias() {
    return alias.orElse("");
  }

  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<? extends T> getJavaType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Collection<?> values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Expression<?>... values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Expression<Collection<?>> values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Object... values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isCompoundSelection() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Predicate isNotNull() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate isNull() {
    // TODO Auto-generated method stub
    return null;
  }

  static class AggregationExpression<N extends Number> extends ExpressionImpl<N> {

    private final SqlAggregation function;
    private final SqlConvertible expression;

    AggregationExpression(@Nonnull final SqlAggregation function, @Nonnull final Expression<?> x) {
      this.function = function;
      this.expression = (SqlConvertible) x;
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
        } catch (ODataJPAModelException e) {
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
      left.asSQL(statement.append(OPENING_BRACKET)).append(" ").append(operation).append(" ");
      return right.asSQL(statement).append(CLOSING_BRACKET);
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
      values.stream().collect(new StringBuilderCollector.ExpressionCollector<T>(statement, ", "));
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
    public StringBuilder asSQL(StringBuilder statement) {

      if (left instanceof FromImpl<?, ?>) {
        try {
          final FromImpl<?, ?> from = ((FromImpl<?, ?>) left);
          statement.append(SqlKeyWords.DISTINCT)
              .append(OPENING_BRACKET);
          from.st.getKey().stream()
              .map(a -> from.get(a.getInternalName()))
              .collect(new StringBuilderCollector.ExpressionCollector<>(statement, ", "));
//              .append(((FromImpl<?, ?>) left).tableAlias.get())
//              .append(DOT)
//              .append("*")
          return statement.append(CLOSING_BRACKET);
        } catch (ODataJPAModelException e) {
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
      args.stream().collect(new StringBuilderCollector.ExpressionCollector<>(statement, ", "));
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
    private Optional<Class<?>> dbType;
    private Optional<JPAPath> jpaPath;

    ParameterExpression(final Integer i, final S value) {
      this.index = i;
      this.value = value;
      this.converter = Optional.empty();
      this.dbType = Optional.empty();
      this.jpaPath = Optional.empty();
    }

    @SuppressWarnings("unchecked")
    T getValue() {
      if (converter.isPresent() && !dbType.orElseThrow(IllegalStateException::new).isAssignableFrom(value.getClass()))
        return converter.get().convertToDatabaseColumn(value);
      if (jpaPath.isPresent() && jpaPath.get().getLeaf().isEnum())
        return (T) ((Number) ((Enum<?>) value).ordinal());
      return (T) value;
    }

    void setPath(@Nullable final Expression<?> x) {
      if (x instanceof PathImpl && ((PathImpl<?>) x).path.isPresent()) {
        jpaPath = Optional.of(((PathImpl<?>) x).path.get()); // NOSONAR
        dbType = Optional.ofNullable(jpaPath.get().getLeaf().getType());
        converter = Optional.ofNullable(jpaPath.get().getLeaf().getConverter());
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

}
