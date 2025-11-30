package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelInternalException;

public class MapCacheFunction<K, V, F, S> implements MapCache<K, V> {
  private Optional<Map<K, V>> value = Optional.empty();
  private final BiFunction<F, S, Map<K, V>> mappingFunction;
  private F first;
  private S second;

  public MapCacheFunction(BiFunction<F, S, Map<K, V>> mappingFunction, F first, S second) {
    super();
    this.mappingFunction = mappingFunction;
    this.first = first;
    this.second = second;
  }

  @Override
  public Map<K, V> get() throws ODataJPAModelException {
    if (value.isEmpty()) {
      try {
        final Map<K, V> newValue = Objects.requireNonNull(mappingFunction.apply(first, second));
        value = Optional.of(newValue);
      } catch (ODataJPAModelInternalException e) {
        throw (ODataJPAModelException) e.getCause();
      }
    }
    return value.orElse(Map.of());
  }
}
