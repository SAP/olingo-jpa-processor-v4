package com.sap.olingo.jpa.processor.core.exception;

public class ODataJPAIllegalArgumentException extends RuntimeException {

  private static final long serialVersionUID = 8012137500100028346L;

  private final String[] params;

  public ODataJPAIllegalArgumentException(final Throwable cause) {
    super(cause);
    params = new String[0];
  }

  public ODataJPAIllegalArgumentException(final String... params) {
    super();
    this.params = params;
  }

  public String[] getParams() {
    return params;
  }
}
