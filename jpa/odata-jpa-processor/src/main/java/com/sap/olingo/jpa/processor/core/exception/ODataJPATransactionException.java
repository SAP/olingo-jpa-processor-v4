package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

public class ODataJPATransactionException extends ODataJPAProcessException { // NOSONAR

  private static final long serialVersionUID = -3720990003700857965L;
  private static final String MESSAGE_KEY = "CANNOT_CREATE_NEW_TRANSACTION";

  public ODataJPATransactionException(final Throwable cause) {
    super(cause, HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

  public ODataJPATransactionException() {
    super(MESSAGE_KEY, HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

  public ODataJPATransactionException(final Exception e) {
    super(MESSAGE_KEY, HttpStatusCode.INTERNAL_SERVER_ERROR, e);
  }

  @Override
  protected String getBundleName() {
    return null;
  }

}
