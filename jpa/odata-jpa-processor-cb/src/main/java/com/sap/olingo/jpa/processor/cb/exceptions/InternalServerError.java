package com.sap.olingo.jpa.processor.cb.exceptions;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class InternalServerError extends RuntimeException {

  private static final long serialVersionUID = -2239224331308130011L;

  public InternalServerError(ODataJPAModelException e) {
    super(e);
  }
}
