package com.sap.olingo.jpa.metadata.core.edm.mapper.api;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public interface JPAEnumerationAttribute {
  <T extends Enum<?>> T enumOf(final String value) throws ODataJPAModelException;

  <T extends Number, E extends Enum<E>> E enumOf(final T value) throws ODataJPAModelException;

  <T extends Number> T valueOf(final String value) throws ODataJPAModelException;

  <T extends Number> T valueOf(final List<String> value) throws ODataJPAModelException;

  boolean isFlags() throws ODataJPAModelException;

  /**
   * Converts a list of string representations either into an array of enumerations, if a converter is given, or
   * otherwise the first value into an enumeration
   * @param value
   * @return
   * @throws ODataJPAModelException
   */
  Object convert(final List<String> values) throws ODataJPAModelException;

}
