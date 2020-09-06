package com.sap.olingo.jpa.processor.core.exception;

import static org.apache.olingo.commons.api.http.HttpStatusCode.NOT_IMPLEMENTED;

/*
 * This exception is thrown when an exception occurs in a jpa pojo method
 */
public class ODataJPANotImplementedException extends ODataJPAProcessException { // NOSONAR

  private static final long serialVersionUID = 2410838419178517426L;
  private static final String BUNDLE_NAME = "processor-exceptions-i18n";

  public ODataJPANotImplementedException(final String... params) {
    super(NOT_IMPLEMENTED.name(), NOT_IMPLEMENTED, params);
  }

  @Override
  protected String getBundleName() {
    return BUNDLE_NAME;
  }

}
