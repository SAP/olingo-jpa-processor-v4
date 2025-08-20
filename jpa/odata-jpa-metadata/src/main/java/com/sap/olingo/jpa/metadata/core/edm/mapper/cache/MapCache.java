package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.Map;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface MapCache<K, V> {

  @Nonnull
  Map<K, V> get() throws ODataJPAModelException;

}