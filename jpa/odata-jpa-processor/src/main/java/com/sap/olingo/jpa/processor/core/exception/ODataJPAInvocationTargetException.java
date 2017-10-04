package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

/*
 * This exception is thrown when an exception occurs in a jpa pojo method
 */
public class ODataJPAInvocationTargetException extends ODataJPAProcessException {

  private static final long   serialVersionUID = 2410838419178517426L;
  private static final String BUNDEL_NAME      = "processor-exceptions-i18n";

  public ODataJPAInvocationTargetException(Throwable throwable) {
    super(throwable, HttpStatusCode.BAD_REQUEST);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

}
