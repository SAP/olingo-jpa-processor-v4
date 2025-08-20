package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.Optional;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface InstanceCache<T> {
  Optional<T> get() throws ODataJPAModelException;
}