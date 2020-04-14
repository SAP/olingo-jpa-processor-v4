package com.sap.olingo.jpa.processor.cb.joiner;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.annotation.Nonnull;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate.BooleanOperator;

public class ExpressionCollector<T> implements Collector<Expression<Boolean>, ExpressionJoiner, Expression<Boolean>> {

  final Supplier<ExpressionJoiner> supplier;

  public ExpressionCollector(@Nonnull final CriteriaBuilder cb, @Nonnull final BooleanOperator operator) {
    this.supplier = () -> new ExpressionJoiner(cb, operator);
  }

  @Override
  public Supplier<ExpressionJoiner> supplier() {
    return supplier;
  }

  @Override
  public BinaryOperator<ExpressionJoiner> combiner() {
    return null;
  }

  @Override
  public BiConsumer<ExpressionJoiner, Expression<Boolean>> accumulator() {
    return ExpressionJoiner::add;
  }

  @Override
  public Function<ExpressionJoiner, Expression<Boolean>> finisher() {
    return ExpressionJoiner::finish;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }
}
