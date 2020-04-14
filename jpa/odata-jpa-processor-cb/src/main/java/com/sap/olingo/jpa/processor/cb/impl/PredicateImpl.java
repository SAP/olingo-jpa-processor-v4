package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import com.sap.olingo.jpa.processor.cb.api.SqlConvertable;
import com.sap.olingo.jpa.processor.cb.api.SqlKeyWords;
import com.sap.olingo.jpa.processor.cb.api.SqlNullCheck;
import com.sap.olingo.jpa.processor.cb.api.SqlSubQuery;

/**
 * 
 * @author D023143
 *
 */
abstract class PredicateImpl extends ExpressionImpl<Boolean> implements Predicate, SqlConvertable {

  protected final List<SqlConvertable> expressions;

  protected PredicateImpl(SqlConvertable... expressions) {
    super();
    this.expressions = Collections.unmodifiableList(Arrays.asList(expressions));
  }

  static Predicate and(Predicate[] restrictions) {
    if (restrictions == null || arrayIsEmpty(restrictions, 2))
      throw new IllegalArgumentException("Parameter 'restrictions' has to have at least 2 elements");
    Predicate p = restrictions[0];
    for (int i = 1; i < restrictions.length; i++) {
      p = new AndPredicate(p, restrictions[i]);
    }
    return p;
  }

  static Predicate or(Predicate[] restrictions) {
    if (restrictions == null || arrayIsEmpty(restrictions, 2))
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

  abstract static class BolleanPredicate extends PredicateImpl {

    BolleanPredicate(final Expression<Boolean> x, final Expression<Boolean> y) {
      super((SqlConvertable) x, (SqlConvertable) y);
    }

    @Override
    public String toString() {
      return "AndPredicate [left=" + expressions.get(0) + ", right=" + expressions.get(1) + "]";
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(OPENING_BRACKET);
      expressions.get(0).asSQL(statment)
          .append(" ")
          .append(getOperator())
          .append(" ");
      expressions.get(1).asSQL(statment);
      statment.append(CLOSING_BRACKET);
      return statment;
    }
  }

  static class AndPredicate extends BolleanPredicate {

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

  static class OrPredicate extends BolleanPredicate {

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

  static class NotPredicate extends PredicateImpl {

    private final SqlConvertable positive;

    NotPredicate(final SqlConvertable predicate) {
      this.positive = predicate;
    }

    @Override
    public boolean isNegated() {
      return true;
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public StringBuilder asSQL(StringBuilder statment) {
      statment
          .append(OPENING_BRACKET)
          .append(SqlKeyWords.NOT)
          .append(" ");
      return positive.asSQL(statment)
          .append(CLOSING_BRACKET);
    }
  }

  static class BetweenExpressionPredicate extends PredicateImpl {

    private final ExpressionImpl<?> attribute;

    BetweenExpressionPredicate(final ExpressionImpl<?> attribute, final Expression<?> left, final Expression<?> right) {
      super((SqlConvertable) left, (SqlConvertable) right);
      this.attribute = attribute;
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(OPENING_BRACKET);
      this.attribute.asSQL(statment)
          .append(" ")
          .append(SqlKeyWords.BETWEEN)
          .append(" ");
      this.expressions.get(0).asSQL(statment)
          .append(" ")
          .append(SqlKeyWords.AND)
          .append(" ");
      return this.expressions.get(1).asSQL(statment).append(CLOSING_BRACKET);
    }
  }

  static class BinaryExpressionPredicate extends PredicateImpl {
    public enum Operation {
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

    private Operation expression;

    BinaryExpressionPredicate(final Operation operation, final Expression<?> left, final Expression<?> right) {
      super((SqlConvertable) left, (SqlConvertable) right);
      this.expression = operation;
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(OPENING_BRACKET);
      this.expressions.get(0).asSQL(statment)
          .append(" ")
          .append(expression)
          .append(" ");
      return this.expressions.get(1).asSQL(statment).append(CLOSING_BRACKET);
    }
  }

  static class LikePredicate extends PredicateImpl {
    private final ParameterExpression<String, ?> pattern;
    private final Optional<ParameterExpression<Character, ?>> escape;

    public LikePredicate(final Expression<String> column, final ParameterExpression<String, ?> pattern) {
      this(column, pattern, Optional.empty());
    }

    public LikePredicate(Expression<String> column, ParameterExpression<String, ?> pattern,
        Optional<ParameterExpression<Character, ?>> escapeChar) {
      super((SqlConvertable) column);
      this.pattern = pattern;
      this.escape = escapeChar;
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public StringBuilder asSQL(final StringBuilder statment) {
      statment.append(OPENING_BRACKET);
      this.expressions.get(0).asSQL(statment)
          .append(" ")
          .append(SqlKeyWords.LIKE)
          .append(" ");
      this.pattern.asSQL(statment);
      this.escape.ifPresent(e -> statment
          .append(" ")
          .append(SqlKeyWords.ESCAPE)
          .append(" "));
      this.escape.ifPresent(e -> e.asSQL(statment));
      return statment.append(CLOSING_BRACKET);
    }
  }

  static class SubQuery extends PredicateImpl {
    private final SqlConvertable query;
    private final SqlSubQuery operator;

    public SubQuery(@Nonnull final Subquery<?> subquery, @Nonnull final SqlSubQuery operator) {
      this.query = (SqlConvertable) Objects.requireNonNull(subquery);
      this.operator = operator;
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public StringBuilder asSQL(@Nonnull final StringBuilder statment) {
      statment.append(operator)
          .append(" ")
          .append(OPENING_BRACKET);
      return query.asSQL(statment).append(CLOSING_BRACKET);
    }

  }

  static class NullPredicate extends PredicateImpl {

    private final SqlNullCheck check;

    NullPredicate(@Nonnull final Expression<?> expression, @Nonnull final SqlNullCheck check) {
      super((SqlConvertable) Objects.requireNonNull(expression));
      this.check = Objects.requireNonNull(check);
    }

    @Override
    public BooleanOperator getOperator() {
      return null;
    }

    @Override
    public StringBuilder asSQL(StringBuilder statment) {
      return expressions.get(0).asSQL(statment.append(OPENING_BRACKET))
          .append(" ").append(check).append(CLOSING_BRACKET);
    }
  }

  @Override
  public Predicate in(Object... values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Expression<?>... values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Collection<?> values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Predicate in(Expression<Collection<?>> values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <X> Expression<X> as(Class<X> type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Selection<Boolean> alias(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isCompoundSelection() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<Selection<?>> getCompoundSelectionItems() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Class<? extends Boolean> getJavaType() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getAlias() {
    // TODO Auto-generated method stub
    return null;
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
  public List<Expression<Boolean>> getExpressions() {
    return asExpression(expressions);
  }

  private List<Expression<Boolean>> asExpression(List<SqlConvertable> sql) {
    return Collections.emptyList();
  }

  @Override
  public Predicate not() {
    return new NotPredicate(this);
  }

}
