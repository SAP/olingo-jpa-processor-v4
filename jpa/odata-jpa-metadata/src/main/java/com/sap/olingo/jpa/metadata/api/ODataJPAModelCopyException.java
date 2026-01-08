package com.sap.olingo.jpa.metadata.api;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class ODataJPAModelCopyException extends RuntimeException {

  private static final long serialVersionUID = -1695284009828517502L;

  public ODataJPAModelCopyException(final String message, final ODataJPAModelException exception) {
    super(message, exception);
  }
}
