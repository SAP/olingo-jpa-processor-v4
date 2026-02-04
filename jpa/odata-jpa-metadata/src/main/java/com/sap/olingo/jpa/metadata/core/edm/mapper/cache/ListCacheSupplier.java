package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException.MessageKeys;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

public class ListCacheSupplier<T> implements ListCache<T> {
  private Optional<List<T>> value = Optional.empty();
  private final Supplier<List<T>> mappingFunction;
  private boolean underConstruction = false;

  public ListCacheSupplier(@Nonnull Supplier<List<T>> mappingFunction) {
    this.mappingFunction = Objects.requireNonNull(mappingFunction);
  }

  @Override
  public List<T> get() throws ODataJPAModelException {
    if (value.isEmpty() && !underConstruction) {
      try {
        underConstruction = true;
        final List<T> newValue = Objects.requireNonNull(mappingFunction.get());
        value = Optional.of(newValue);
        underConstruction = false;
      } catch (ODataJPAModelInternalException e) {
        throw (ODataJPAModelException) e.getCause();
      }
    }
    if (underConstruction)
      throw new ODataJPAModelException(MessageKeys.CYCLE_DETECTED);
    return value.orElse(List.of());
  }
}
