package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAEnumerationAttribute {
  Enum<?> valueOf(final String value) throws ODataJPAModelException;

  <T extends Number> Enum<?> valueOf(T value) throws ODataJPAModelException;
}
