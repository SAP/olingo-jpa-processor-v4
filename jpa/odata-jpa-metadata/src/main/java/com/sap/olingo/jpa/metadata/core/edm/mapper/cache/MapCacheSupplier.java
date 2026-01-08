package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

public class MapCacheSupplier<K, V> implements MapCache<K, V> {
  private Optional<Map<K, V>> value = Optional.empty();
  private final Supplier<Map<K, V>> mappingFunction;

  public MapCacheSupplier(Supplier<Map<K, V>> mappingFunction) {
    super();
    this.mappingFunction = mappingFunction;
  }

  @Override
  public Map<K, V> get() throws ODataJPAModelException {
    if (value.isEmpty()) {
      try {
        final Map<K, V> newValue = Objects.requireNonNull(mappingFunction.get());
        value = Optional.of(newValue);
      } catch (ODataJPAModelInternalException e) {
        throw (ODataJPAModelException) e.getCause();
      }
    }
    return value.orElse(Map.of());
  }
}
