package com.sap.olingo.jpa.processor.cb.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.persistence.criteria.Expression;

import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;

class ParameterBuffer {
  private int index = 1;
  private final Map<Integer, ParameterExpression<?, ?>> parameter;

  ParameterBuffer() {
    super();
    parameter = new HashMap<>();
  }

  <T, S> ParameterExpression<T, S> addValue(@Nonnull final S value) {
    return this.addValue(value, null);
  }

  <T, S> ParameterExpression<T, S> addValue(@Nonnull final S value, final Expression<?> x) {

    final ParameterExpression<T, S> param = new ParameterExpression<>(index, Objects.requireNonNull(value));
    param.setPath(x);
    this.parameter.put(index++, param);
    return param;
  }

  Map<Integer, ParameterExpression<?, ?>> getParameter() {
    return parameter;
  }
}
