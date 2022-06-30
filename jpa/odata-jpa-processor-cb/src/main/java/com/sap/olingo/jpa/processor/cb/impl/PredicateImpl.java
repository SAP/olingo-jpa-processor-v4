package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import com.sap.olingo.jpa.processor.cb.exeptions.NotImplementedException;
import com.sap.olingo.jpa.processor.cb.joiner.SqlConvertible;
import com.sap.olingo.jpa.processor.cb.joiner.StringBuilderCollector;

/**
 *
 * @author Oliver Grande
 *
 */
abstract class PredicateImpl extends ExpressionImpl<Boolean> implements Predicate {

  private static final int REQUIRED_NO_OPERATOR = 2;
  protected final List<SqlConvertible> expressions;

  static Predicate and(final Predicate[] restrictions) {
    if (restrictions == null || arrayIsEmpty(restrictions, REQUIRED_NO_OPERATOR))
      throw new IllegalArgumentException("Parameter 'restrictions' has to have at least 2 elements");
    Predicate p = restrictions[0];
    for (int i = 1; i < restrictions.length; i++) {
      p = new AndPredicate(p, restrictions[i]);
    }
    return p;
  }

  static Predicate or(final Predicate[] restrictions) {
    if (restrictions == null || arrayIsEmpty(restrictions, REQUIRED_NO_OPERATOR))
      throw new IllegalArgumentException("Parameter 'restrictions' has to have at least 2 elements");
    Predicate p = restrictions[0];
    for (int i = 1; i < restrictions.length; i++) {
      p = new OrPredicate(p, restrictions[i]);
    }
    return p;
  }

  private static boolean arrayIsEmpty(final Predicate[] restrictions, final int requiredNoElements) {
    for (int i = 0; i < restrictions.length; i++) {
      if (restrictions[i] == null)
        return true;
    }
    return restrictions.length < requiredNoElements;
  }

  protected PredicateImpl(final SqlConvertible... expressions) {
    super();
    this.expressions = Collections.unmodifiableList(Arrays.asList(expressions));
  }

  @Override
  public Selection<Boolean> alias(final String name) {
    alias = Optional.ofNullable(name);
    return this;
  }

  @Override
  public <X> Expression<X> as(final Class<X> type) {
    throw new NotImplementedException();
  }

  @Override
  @CheckForNull
  public String getAlias() {
    return alias.orElse(null);
  }

  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    throw new NotImplementedException();
  }

  @Override
  public List<Expression<Boolean>> getExpressions() {
    return asExpression();
  }

  @Override
  public Class<? extends Boolean> getJavaType() {
    throw new NotImplementedException();
  }

  /**
   * Whether the predicate has been created from another
   * predicate by applying the <code>Predicate.not()</code> method
   * or the <code>CriteriaBuilder.not()</code> method.
   * @return boolean indicating if the predicate is
   * a negated predicate
   */
  @Override
  public boolean isNegated() {
    return false;
  }

  @Override
  public Predicate not() {
    return new NotPredicate(this);
  }

  private List<Expression<Boolean>> asExpression() {
    return Collections.emptyList();
  }

  @Override
  public String toString() {
    return "PredicateImpl [sql=" + asSQL(new StringBuilder()) + "]";
  }

  static class AndPredicate extends BooleanPredicate {

    AndPredicate(final Expression<Boolean> x, final Expression<Boolean> y) {
      super(x, y);
    }

    /**
     * Return the boolean operator for the predicate.
     * If the predicate is simple, this is <code>AND</code>.
     * @return boolean operator for the predicate
     */
    @Override
    public BooleanOperator getOperator() {
      return Predicate.BooleanOperator.AND;
    }
  }

  static class BetweenExpressionPredicate extends PredicateImpl {

    private final ExpressionImpl<?> attribute;

    BetweenExpressionPredicate(final ExpressionImpl<?> attribute, final Expression<?> left, final Expression<?> right) {
      super((SqlConvertible) left, (SqlConvertible) right);
      this.attribute = attribute;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(OPENING_BRACKET);
      this.attribute.asSQL(statement)
          .append(" ")
          .append(SqlKeyWords.BETWEEN)
          .append(" ");
      this.expressions.get(0).asSQL(statement)
          .append(" ")
          .append(SqlKeyWords.AND)
          .append(" ");
      return this.expressions.get(1).asSQL(statement).append(CLOSING_BRACKET);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }
  }

  static class BinaryExpressionPredicate extends PredicateImpl {
    private final Operation expression;

    BinaryExpressionPredicate(final Operation operation, final Expression<?> left, final Expression<?> right) {
      super((SqlConvertible) left, (SqlConvertible) right);
      this.expression = operation;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(OPENING_BRACKET);
      this.expressions.get(0).asSQL(statement)
          .append(" ")
          .append(expression)
          .append(" ");
      return this.expressions.get(1).asSQL(statement).append(CLOSING_BRACKET);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    enum Operation {
      EQ("="), NE("<>"), GT(">"), GE(">="), LT("<"), LE("<=");

      private String keyWord;

      private Operation(final String keyWord) {
        this.keyWord = keyWord;
      }

      @Override
      public String toString() {
        return keyWord;
      }
    }
  }

  abstract static class BooleanPredicate extends PredicateImpl {

    BooleanPredicate(final Expression<Boolean> x, final Expression<Boolean> y) {
      super((SqlConvertible) x, (SqlConvertible) y);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(OPENING_BRACKET);
      expressions.get(0).asSQL(statement)
          .append(" ")
          .append(getOperator())
          .append(" ");
      expressions.get(1).asSQL(statement);
      statement.append(CLOSING_BRACKET);
      return statement;
    }

    @Override
    public String toString() {
      return "AndPredicate [left=" + expressions.get(0) + ", right=" + expressions.get(1) + "]";
    }
  }

  static class LikePredicate extends PredicateImpl {
    private final ParameterExpression<String, ?> pattern;
    private final Optional<ParameterExpression<Character, ?>> escape;

    public LikePredicate(final Expression<String> column, final ParameterExpression<String, ?> pattern) {
      this(column, pattern, Optional.empty());
    }

    public LikePredicate(final Expression<String> column, final ParameterExpression<String, ?> pattern,
        final Optional<ParameterExpression<Character, ?>> escapeChar) {
      super((SqlConvertible) column);
      this.pattern = pattern;
      this.escape = escapeChar;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(OPENING_BRACKET);
      this.expressions.get(0).asSQL(statement)
          .append(" ")
          .append(SqlKeyWords.LIKE)
          .append(" ");
      this.pattern.asSQL(statement);
      this.escape.ifPresent(e -> statement
          .append(" ")
          .append(SqlKeyWords.ESCAPE)
          .append(" "));
      this.escape.ifPresent(e -> e.asSQL(statement));
      return statement.append(CLOSING_BRACKET);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }
  }

  static class NotPredicate extends PredicateImpl {

    private final SqlConvertible positive;

    NotPredicate(final SqlConvertible predicate) {
      this.positive = predicate;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement
          .append(OPENING_BRACKET)
          .append(SqlKeyWords.NOT)
          .append(" ");
      return positive.asSQL(statement)
          .append(CLOSING_BRACKET);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public boolean isNegated() {
      return true;
    }
  }

  static class NullPredicate extends PredicateImpl {

    private final SqlNullCheck check;

    NullPredicate(@Nonnull final Expression<?> expression, @Nonnull final SqlNullCheck check) {
      super((SqlConvertible) Objects.requireNonNull(expression));
      this.check = Objects.requireNonNull(check);
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      return expressions.get(0).asSQL(statement.append(OPENING_BRACKET))
          .append(" ").append(check).append(CLOSING_BRACKET);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }
  }

  static class OrPredicate extends BooleanPredicate {

    OrPredicate(final Expression<Boolean> x, final Expression<Boolean> y) {
      super(x, y);
    }

    /**
     * Return the boolean operator for the predicate.
     * If the predicate is simple, this is <code>AND</code>.
     * @return boolean operator for the predicate
     */
    @Override
    public BooleanOperator getOperator() {
      return Predicate.BooleanOperator.OR;
    }
  }

  static class In<X> extends PredicateImpl implements CriteriaBuilder.In<X> {
    private final List<Path<? extends X>> paths;

    In(final List<Path<? extends X>> paths, final Subquery<?> subquery) {
      super((SqlConvertible) Objects.requireNonNull(subquery));
      this.paths = paths;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statement) {
      statement.append(OPENING_BRACKET);
      statement.append(paths
          .stream()
          .map(p -> ((Expression<?>) p)) // NOSONAR
          .collect(new StringBuilderCollector.ExpressionCollector(statement, ", ")));
      statement.append(CLOSING_BRACKET)
          .append(" ")
          .append(SqlKeyWords.IN)
          .append(" ")
          .append(OPENING_BRACKET);
      final SqlConvertible sub = expressions.get(0);
      return sub.asSQL(statement).append(CLOSING_BRACKET);
    }

    @Override
    public javax.persistence.criteria.CriteriaBuilder.In<X> value(final X value) {
      throw new NotImplementedException();
    }

    @Override
    public javax.persistence.criteria.CriteriaBuilder.In<X> value(final Expression<? extends X> value) {
      throw new NotImplementedException();
    }

    @Override
    public BooleanOperator getOperator() {
      return BooleanOperator.AND;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Expression<X> getExpression() {
      return paths.isEmpty() ? null : (Expression<X>) paths.get(0);
    }

  }

  static class SubQuery extends PredicateImpl {
    private final SqlConvertible query;
    private final SqlSubQuery operator;

    public SubQuery(@Nonnull final Subquery<?> subquery, @Nonnull final SqlSubQuery operator) {
      this.query = (SqlConvertible) Objects.requireNonNull(subquery);
      this.operator = operator;
    }

    @Override
    public StringBuilder asSQL(@Nonnull final StringBuilder statement) {
      statement.append(operator)
          .append(" ")
          .append(OPENING_BRACKET);
      return query.asSQL(statement).append(CLOSING_BRACKET);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

  }
}
