package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * This exception is thrown when an exception occurs in a jpa pojo method
 */
public class ODataJPAInvocationTargetException extends ODataJPAProcessException {

  private static final long serialVersionUID = 2410838419178517426L;
  private static final String BUNDEL_NAME = "processor-exceptions-i18n";
  private final String path;

  enum MessageKeys implements ODataJPAMessageKey {
    WRONG_VALUE;
    @Override
    public String getKey() {
      return name();
    }
  }

  public ODataJPAInvocationTargetException(Throwable e, final String path) {
    super(e, HttpStatusCode.BAD_REQUEST);
    this.path = path;
  }

  public ODataJPAInvocationTargetException(Throwable e) {
    super(e, HttpStatusCode.BAD_REQUEST);
    this.path = null;
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

  public String getPath() {
    return path;
  }

}
