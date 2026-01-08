package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

public class ODataJPABatchException extends ODataJPAProcessException { // NOSONAR
  /**
   *
   */
  private static final long serialVersionUID = 8492368933922574285L;
  private static final String BUNDLE_NAME = "batch-parallel-exceptions-i18n";
  private static final String MESSAGE_KEY = "UNSUPPORTED_BATCH_PARTS";

  public ODataJPABatchException(final HttpStatusCode statusCode) {
    super(MESSAGE_KEY, statusCode);
  }

  public ODataJPABatchException(final ODataJPABatchRuntimeException e) {
    super(e.getCause(), HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

  @Override
  protected String getBundleName() {
    return BUNDLE_NAME;
  }

}
