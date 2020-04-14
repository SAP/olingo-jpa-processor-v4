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
import com.sap.olingo.jpa.processor.cb.api.SqlConvertable;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;
import com.sap.olingo.jpa.processor.cb.api.SqlStringFunctions;
import com.sap.olingo.jpa.processor.cb.api.SqlSubQuery;
import com.sap.olingo.jpa.processor.cb.api.SqlTimeFunctions;
import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

abstract class ExpressionImpl<T> implements Expression<T>, SqlConvertable {

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
    private final SqlConvertable expression;

    AggregationExpression(@Nonnull final SqlAggregation function, @Nonnull final Expression<?> x) {
      this.function = function;
      this.expression = (SqlConvertable) x;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(function)
          .append(OPENING_BRACKET);
      return expression(statment)
          .append(CLOSING_BRACKET);
    }

    private StringBuilder expression(final StringBuilder statment) {
      if (expression instanceof FromImpl<?, ?>) {
        final FromImpl<?, ?> from = (FromImpl<?, ?>) expression;
        final String tableAlias = from.tableAlias.orElseThrow(IllegalStateException::new);
        try {
          final List<JPAAttribute> keys = from.st.getKey();
          statment
              .append(tableAlias)
              .append(DOT)
              .append(from.st.getPath(keys.get(0).getExternalName()).getDBFieldName());
        } catch (ODataJPAModelException e) {
          throw new IllegalArgumentException(e);
        }
        return statment;
      } else {
        return expression.asSQL(statment);
      }
    }

    SqlConvertable getExpression() {
      return expression;
    }
  }

  static class ArithmeticExpression<N extends Number> extends ExpressionImpl<N> {

    private final SqlConvertable left;
    private final SqlConvertable right;
    private final SqlArithmetic operation;

    ArithmeticExpression(@Nonnull final Expression<? extends N> x, @Nonnull final Expression<? extends N> y,
        @Nonnull final SqlArithmetic operation) {

      this.left = (SqlConvertable) Objects.requireNonNull(x);
      this.right = (SqlConvertable) Objects.requireNonNull(y);
      this.operation = Objects.requireNonNull(operation);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      left.asSQL(statment.append(OPENING_BRACKET)).append(" ").append(operation).append(" ");
      return right.asSQL(statment).append(CLOSING_BRACKET);
    }

  }

  static class CoalesceExpression<T> extends ExpressionImpl<T> implements Coalesce<T> {
    private final List<Expression<T>> values;

    CoalesceExpression() {
      super();
      this.values = new ArrayList<>();
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {

      statment.append(SqlKeyWords.COALESCE)
          .append(OPENING_BRACKET);
      values.stream().collect(new StringBuilderCollector.ExpressionCollector<T>(statment, ", "));
      return statment.append(CLOSING_BRACKET);
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
    private final SqlConvertable first;
    private final SqlConvertable second;

    ConcatExpression(@Nonnull final Expression<String> first, @Nonnull final Expression<String> second) {
      this.first = (SqlConvertable) Objects.requireNonNull(first);
      this.second = (SqlConvertable) Objects.requireNonNull(second);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(SqlStringFunctions.CONCAT)
          .append(OPENING_BRACKET);
      first.asSQL(statment)
          .append(", ");
      second.asSQL(statment);
      return statment.append(CLOSING_BRACKET);
    }
  }

  static class DistinctExpression<T> extends ExpressionImpl<T> {
    private final SqlConvertable left;

    DistinctExpression(@Nonnull final Expression<?> x) {
      this.left = (SqlConvertable) Objects.requireNonNull(x);
    }

    @Override
    public StringBuilder asSQL(StringBuilder statment) {

      if (left instanceof FromImpl<?, ?>) {
        try {
          final FromImpl<?, ?> from = ((FromImpl<?, ?>) left);
          statment.append(SqlKeyWords.DISTINCT)
              .append(OPENING_BRACKET);
          from.st.getKey().stream()
              .map(a -> from.get(a.getInternalName()))
              .collect(new StringBuilderCollector.ExpressionCollector<>(statment, ", "));
//              .append(((FromImpl<?, ?>) left).tableAlias.get())
//              .append(DOT)
//              .append("*")
          return statment.append(CLOSING_BRACKET);
        } catch (ODataJPAModelException e) {
          throw new IllegalStateException(e);
        }

      }
      return left.asSQL(statment.append(SqlKeyWords.DISTINCT).append(OPENING_BRACKET)).append(CLOSING_BRACKET);
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
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(functionName).append(OPENING_BRACKET);
      args.stream().collect(new StringBuilderCollector.ExpressionCollector<>(statment, ", "));
      return statment.append(CLOSING_BRACKET);
    }

    @Override
    public Class<? extends T> getJavaType() {
      return type;
    }

  }

  static class LocateExpression extends ExpressionImpl<Integer> {
    private final SqlConvertable expression;
    private final SqlConvertable pattern;
    private final Optional<SqlConvertable> from;

    LocateExpression(@Nonnull final Expression<String> x, @Nonnull final Expression<String> pattern,
        final Expression<Integer> from) {
      this.expression = (SqlConvertable) Objects.requireNonNull(x);
      this.pattern = (SqlConvertable) Objects.requireNonNull(pattern);
      this.from = Optional.ofNullable((SqlConvertable) from);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      // LOCATE(<pattern>, <string>, <from>)
      statment.append(SqlStringFunctions.LOCATE)
          .append(OPENING_BRACKET);
      pattern.asSQL(statment)
          .append(", ");
      expression.asSQL(statment);
      from.ifPresent(l -> l.asSQL(statment.append(", ")));
      return statment.append(CLOSING_BRACKET);
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
    public StringBuilder asSQL(final StringBuilder statment) {
      return statment.append("?").append(index.toString());
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

    private final SqlConvertable query;
    private final SqlSubQuery operator;

    SubQuery(@Nonnull final Subquery<?> subquery, @Nonnull final SqlSubQuery operator) {
      this.query = (SqlConvertable) Objects.requireNonNull(subquery);
      this.operator = operator;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(operator)
          .append(" ")
          .append(OPENING_BRACKET);
      return query.asSQL(statment).append(CLOSING_BRACKET);
    }

  }

  static class SubstringExpression extends ExpressionImpl<String> {
    private final SqlConvertable expression;
    private final SqlConvertable from;
    private final Optional<SqlConvertable> len;

    SubstringExpression(@Nonnull final Expression<String> x, @Nonnull final Expression<Integer> from,
        final Expression<Integer> len) {
      this.expression = (SqlConvertable) Objects.requireNonNull(x);
      this.from = (SqlConvertable) Objects.requireNonNull(from);
      this.len = Optional.ofNullable((SqlConvertable) len);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(SqlStringFunctions.SUBSTRING)
          .append(OPENING_BRACKET);
      expression.asSQL(statment)
          .append(", ");
      from.asSQL(statment);
      len.ifPresent(l -> l.asSQL(statment.append(", ")));
      return statment.append(CLOSING_BRACKET);
    }
  }

  static class TimeExpression<T> extends ExpressionImpl<T> {

    private final SqlTimeFunctions function;

    TimeExpression(@Nonnull final SqlTimeFunctions function) {
      this.function = Objects.requireNonNull(function);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      return statment.append(function);
    }

  }

  static class UnaryFunctionalExpression<T> extends ExpressionImpl<T> {
    private final SqlConvertable left;
    private final SqlStringFunctions function;

    UnaryFunctionalExpression(@Nonnull final Expression<?> x, @Nonnull final SqlStringFunctions function) {
      this.left = (SqlConvertable) Objects.requireNonNull(x);
      this.function = Objects.requireNonNull(function);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(function).append(OPENING_BRACKET);
      return left.asSQL(statment).append(CLOSING_BRACKET);
    }

  }

}
