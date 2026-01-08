package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

public class InstanceCacheFunction<T, I, B> implements InstanceCache<T> {
  private Optional<Optional<T>> value = Optional.empty();
  private final BiFunction<B, I, T> mappingFunction;
  private B first;
  private I second;

  public InstanceCacheFunction(@Nonnull BiFunction<B, I, T> mappingFunction, B first, I second) {
    this.mappingFunction = Objects.requireNonNull(mappingFunction);
    this.first = first;
    this.second = second;
  }

  @Override
  public Optional<T> get() throws ODataJPAModelException {
    if (value.isEmpty()) {
      try {
        final T newValue = mappingFunction.apply(first, second);
        value = Optional.of(Optional.ofNullable(newValue));
      } catch (ODataJPAModelInternalException e) {
        throw (ODataJPAModelException) e.getCause();
      }
    }
    return value.get();
  }

}
