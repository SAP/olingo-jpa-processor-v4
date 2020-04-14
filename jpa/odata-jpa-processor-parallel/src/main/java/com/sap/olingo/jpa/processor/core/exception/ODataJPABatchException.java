package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

public class ODataJPABatchException extends ODataJPAProcessException { // NOSONAR
  /**
   * 
   */
  private static final long serialVersionUID = 8492368933922574285L;
  private static final String BUNDEL_NAME = "processor-exceptions-i18n";

  public enum MessageKeys implements ODataJPAMessageKey {
    UNSUPPORTED_BATCH_PARTS;

    @Override
    public String getKey() {
      return name();
    }
  }

  public ODataJPABatchException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
    super(messageKey.getKey(), statusCode);
  }

  public ODataJPABatchException(final ODataJPABatchRuntimeException e) {
    super(e.getCause(), HttpStatusCode.INTERNAL_SERVER_ERROR);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

}
