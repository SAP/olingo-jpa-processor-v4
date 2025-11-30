package com.sap.olingo.jpa.metadata.core.edm.mapper.cache;

import java.util.List;

import javax.annotation.Nonnull;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface ListCache<T> {
  @Nonnull
  List<T> get() throws ODataJPAModelException;
}