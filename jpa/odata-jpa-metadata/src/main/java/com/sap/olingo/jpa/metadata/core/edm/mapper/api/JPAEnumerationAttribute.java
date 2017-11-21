package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAEnumerationAttribute {
  <T extends Enum<?>> T enumOf(final String value) throws ODataJPAModelException;

  <T extends Number> Enum<?> enumOf(final T value) throws ODataJPAModelException;

  <E extends Enum<?>, T extends Number> T valueOf(final String value) throws ODataJPAModelException;
}
