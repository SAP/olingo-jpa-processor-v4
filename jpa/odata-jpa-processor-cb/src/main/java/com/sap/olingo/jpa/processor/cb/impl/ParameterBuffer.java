package com.sap.olingo.jpa.processor.cb.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import jakarta.persistence.criteria.Expression;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;

class ParameterBuffer {
  private static final Log LOG = LogFactory.getLog(ParameterBuffer.class);
  private int index = 1;
  private final Map<Integer, ParameterExpression<Object, Object>> parameterByHash;

  ParameterBuffer() {
    super();
    parameterByHash = new HashMap<>();
  }

  <T, S> ParameterExpression<T, S> addValue(@Nonnull final S value) {
    return this.addValue(value, null);
  }

  @SuppressWarnings("unchecked")
  <T, S> ParameterExpression<T, S> addValue(@Nonnull final S value, final Expression<?> expression) {

    ParameterExpression<T, S> param = new ParameterExpression<>(index, Objects.requireNonNull(value), expression);
    if (!parameterByHash.containsKey(param.hashCode())) {
      parameterByHash.put(param.hashCode(), (ParameterExpression<Object, Object>) param);
      index++;
    } else {
      // Hibernate does not allow provisioning of parameter that are not used in a query
      param = (ParameterExpression<T, S>) parameterByHash.get(param.hashCode());
      LOG.trace("Parameter found in buffer: " + param);
    }
    return param;
  }

  Map<Integer, ParameterExpression<Object, Object>> getParameter() {
    return parameterByHash;
  }
}
