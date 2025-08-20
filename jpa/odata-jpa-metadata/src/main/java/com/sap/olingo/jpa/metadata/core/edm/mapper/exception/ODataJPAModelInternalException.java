package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

public class ODataJPAModelInternalException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ODataJPAModelInternalException(final ODataJPAModelException rootCause) {
    super(rootCause);
  }
}
