package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

public class InstanceCacheSupplier<T> implements InstanceCache<T> {
  private Optional<Optional<T>> value = Optional.empty();
  private final Supplier<T> mappingFunction;

  public InstanceCacheSupplier(@Nonnull Supplier<T> mappingFunction) {
    this.mappingFunction = Objects.requireNonNull(mappingFunction);
  }

  @Override
  public Optional<T> get() throws ODataJPAModelException {
    if (value.isEmpty()) {
      try {
        final T newValue = mappingFunction.get();
        value = Optional.of(Optional.ofNullable(newValue));
      } catch (ODataJPAModelInternalException e) {
        throw (ODataJPAModelException) e.getCause();
      }
    }
    return value.get();
  }

}
