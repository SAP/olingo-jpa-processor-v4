package com.sap.olingo.jpa.processor.cb.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Parameter;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;

import com.sap.olingo.jpa.processor.cb.impl.ExpressionImpl.ParameterExpression;

abstract class AbstractQueryImpl implements Query {
  static final String SET_A_PARAMETER_EXCEPTION =
      "Set a parameter is not supported. Parameter have to be forwarded from criteria query";

  private final ParameterBuffer parameterBuffer;
  private Optional<LockModeType> lockMode;
  private final Map<String, Object> hints;
  private Optional<FlushModeType> flushMode;

  AbstractQueryImpl(final ParameterBuffer parameterBuffer) {
    super();
    this.parameterBuffer = parameterBuffer;
    this.hints = new HashMap<>();
    this.lockMode = Optional.empty();
    this.flushMode = Optional.empty();
  }

  @CheckForNull
  @Override
  public LockModeType getLockMode() {
    return lockMode.orElse(null);
  }

  @Override
  public Query setLockMode(final LockModeType lockMode) {
    this.lockMode = Optional.ofNullable(lockMode);
    return this;
  }

  @CheckForNull
  @Override
  public FlushModeType getFlushMode() {
    return flushMode.orElse(null);
  }

  @Override
  public Query setFlushMode(final FlushModeType flushMode) {
    this.flushMode = Optional.ofNullable(flushMode);
    return this;
  }

  @Override
  public Map<String, Object> getHints() {
    return this.hints;
  }

  @Override
  public Query setHint(final String hintName, final Object value) {
    this.hints.put(hintName, value);
    return this;
  }

  /**
   * Return a boolean indicating whether a value has been bound
   * to the parameter.
   * @param param parameter object
   * @return boolean indicating whether parameter has been bound
   * @since 2.0
   */
  @Override
  public boolean isBound(final Parameter<?> param) {
    return parameterBuffer.getParameters().containsValue(param);
  }

  /**
   * Get the parameter object corresponding to the declared
   * positional parameter with the given position.
   * This method is not required to be supported for native
   * queries.
   * @param position position
   * @return parameter object
   * @throws IllegalArgumentException if the parameter with the
   * specified position does not exist
   * @throws IllegalStateException if invoked on a native
   * query when the implementation does not support
   * this use
   * @since 2.0
   */
  @Override
  public Parameter<?> getParameter(final int position) {
    final Optional<?> parameter = parameterBuffer
        .getParameters()
        .values()
        .stream()
        .filter(p -> p.getPosition().equals(position))
        .findFirst();
    return (Parameter<?>) parameter
        .orElseThrow(() -> new IllegalArgumentException("No parameter with index " + position));
  }

  /**
   * Get the parameter object corresponding to the declared
   * positional parameter with the given position and type.
   * This method is not required to be supported by the provider.
   * @param position position
   * @param type type
   * @return parameter object
   * @throws IllegalArgumentException if the parameter with the
   * specified position does not exist or is not assignable
   * to the type
   * @throws IllegalStateException if invoked on a native
   * query or Jakarta Persistence query language query when
   * the implementation does not support this use
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> Parameter<X> getParameter(final int position, final Class<X> type) {
    final var parameter = getParameter(position);
    if (parameter.getParameterType() != null && type != null && !type.isAssignableFrom(parameter.getParameterType()))
      throw new IllegalArgumentException("Parameter at " + position + " has different type");
    return (Parameter<X>) parameter;
  }

  /**
   * Get the parameter object corresponding to the declared
   * parameter of the given name.
   * This method is not required to be supported for native
   * queries.
   * @param name parameter name
   * @return parameter object
   * @throws IllegalArgumentException if the parameter of the
   * specified name does not exist
   * @throws IllegalStateException if invoked on a native
   * query when the implementation does not support
   * this use
   * @since 2.0
   */
  @Override
  public Parameter<?> getParameter(final String name) {
    final var position = Integer.valueOf(name);
    return getParameter(position);
  }

  /**
   * Get the parameter object corresponding to the declared
   * parameter of the given name and type.
   * This method is required to be supported for criteria queries
   * only.
   * @param name parameter name
   * @param type type
   * @return parameter object
   * @throws IllegalArgumentException if the parameter of the
   * specified name does not exist or is not assignable
   * to the type
   * @throws IllegalStateException if invoked on a native
   * query or Jakarta Persistence query language query when
   * the implementation does not support this use
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> Parameter<X> getParameter(final String name, final Class<X> type) {
    final var parameter = getParameter(name);
    if (parameter.getParameterType() != null && type != null && !type.isAssignableFrom(parameter.getParameterType()))
      throw new IllegalArgumentException("Parameter with name " + name + " has different type");
    return (Parameter<X>) parameter;
  }

  /**
   * Get the parameter objects corresponding to the declared
   * parameters of the query.
   * Returns empty set if the query has no parameters.
   * This method is not required to be supported for native
   * queries.
   * @return set of the parameter objects
   * @throws IllegalStateException if invoked on a native
   * query when the implementation does not support
   * this use
   * @since 2.0
   */
  @Override
  public Set<Parameter<?>> getParameters() {
    return parameterBuffer.getParameters()
        .values().stream().collect(Collectors.toSet());
  }

  /**
   * Return the input value bound to the positional parameter.
   * (Note that OUT parameters are unbound.)
   * @param position position
   * @return parameter value
   * @throws IllegalStateException if the parameter has not been
   * been bound
   * @throws IllegalArgumentException if the parameter with the
   * specified position does not exist
   * @since 2.0
   */
  @Override
  public Object getParameterValue(final int position) {
    final Optional<?> parameter = parameterBuffer
        .getParameters()
        .values()
        .stream()
        .filter(p -> p.getPosition().equals(position))
        .findFirst();
    return parameter.orElseThrow(() -> new IllegalArgumentException("No parameter with index " + position));
  }

  /**
   * Return the input value bound to the parameter.
   * (Note that OUT parameters are unbound.)
   * @param param parameter object
   * @return parameter value
   * @throws IllegalArgumentException if the parameter is not
   * a parameter of the query
   * @throws IllegalStateException if the parameter has not been
   * been bound
   * @since 2.0
   */
  @SuppressWarnings("unchecked")
  @Override
  public <X> X getParameterValue(final Parameter<X> param) {
    final Optional<ParameterExpression<Object, Object>> parameter = parameterBuffer.getParameters().values()
        .stream()
        .filter(p -> p.equals(param))
        .findFirst();

    return (X) parameter.map(ParameterExpression::getValue)
        .orElseThrow(() -> new IllegalArgumentException("Parameter unknown " + param));
  }

  @Override
  public Object getParameterValue(final String name) {
    final ParameterExpression<?, ?> parameter = (ParameterExpression<?, ?>) getParameter(name);
    return parameter.getValue();
  }

  @Override
  public Query setParameter(final int position, final Calendar value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final int position, final Date value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final int position, final Object value) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final Parameter<Calendar> param, final Calendar value,
      final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final Parameter<Date> param, final Date value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public <X> Query setParameter(final Parameter<X> param, final X value) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final String name, final Calendar value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final String name, final Date value, final TemporalType temporalType) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  @Override
  public Query setParameter(final String name, final Object value) {
    throw new IllegalStateException(SET_A_PARAMETER_EXCEPTION);
  }

  void copyParameter(final Query query) {
    parameterBuffer.getParameters().entrySet().stream().forEach(entry -> query.setParameter(entry.getValue()
        .getPosition(), entry.getValue().getValue()));
  }
}
