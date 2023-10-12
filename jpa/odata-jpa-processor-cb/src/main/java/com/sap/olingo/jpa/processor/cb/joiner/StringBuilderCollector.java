package com.sap.olingo.jpa.processor.cb.joiner;

import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Selection;

public abstract class StringBuilderCollector<T> implements Collector<T, StringBuilderJoiner<T>, String> {

  final Supplier<StringBuilderJoiner<T>> supplier;

  StringBuilderCollector(@Nonnull final StringBuilder statement, @Nonnull final String delimiter) {
    this.supplier = () -> new StringBuilderJoiner<>(statement, delimiter);
  }

  @Override
  public Supplier<StringBuilderJoiner<T>> supplier() {
    return supplier;
  }

  @Override
  public BinaryOperator<StringBuilderJoiner<T>> combiner() {
    return null;
  }

  @Override
  public BiConsumer<StringBuilderJoiner<T>, T> accumulator() {
    return StringBuilderJoiner::add;
  }

  @Override
  public Function<StringBuilderJoiner<T>, String> finisher() {
    return StringBuilderJoiner::finish;
  }

  @Override
  public Set<Characteristics> characteristics() {
    return Collections.emptySet();
  }

  public static class OrderCollector extends StringBuilderCollector<Order> {
    public OrderCollector(@Nonnull final StringBuilder statement, @Nonnull final String delimiter) {
      super(statement, delimiter);
    }
  }

  public static class ExpressionCollector extends StringBuilderCollector<Expression<?>> {
    public ExpressionCollector(@Nonnull final StringBuilder statement, @Nonnull final String delimiter) {
      super(statement, delimiter);
    }
  }

  public static class SelectionCollector extends StringBuilderCollector<Selection<?>> {
    public SelectionCollector(@Nonnull final StringBuilder statement, @Nonnull final String delimiter) {
      super(statement, delimiter);
    }
  }
}
