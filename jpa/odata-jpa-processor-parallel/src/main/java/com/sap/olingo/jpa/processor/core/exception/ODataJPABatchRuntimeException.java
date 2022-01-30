package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.ex.ODataException;

public class ODataJPABatchRuntimeException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -4042442300828284445L;

  public ODataJPABatchRuntimeException(ODataException e) {
    super(e);
  }
}
