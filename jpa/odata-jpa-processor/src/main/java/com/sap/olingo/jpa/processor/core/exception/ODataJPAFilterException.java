package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

public class ODataJPAFilterException extends ODataJPAProcessException {
  /**
   *
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public enum MessageKeys implements ODataJPAMessageKey {
    NOT_SUPPORTED_OPERATOR,
    NOT_SUPPORTED_FILTER,
    NOT_SUPPORTED_OPERATOR_TYPE,
    NOT_SUPPORTED_FUNCTION_COLLECTION,
    NOT_SUPPORTED_FUNCTION_NOT_SCALAR,
    NOT_SUPPORTED_TRANSIENT,
    NOT_ALLOWED_MEMBER,

    FILTERING_REQUIRED,
    FILTERING_NOT_SUPPORTED,
    FILTERING_MISSING_PROPERTIES,

    NO_VALUES_OUT_OF_LIMIT;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDLE_NAME = "processor-exceptions-i18n";

  public ODataJPAFilterException(final Throwable cause, final HttpStatusCode statusCode) {
    super(cause, statusCode);
  }

  public ODataJPAFilterException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final Throwable cause, final String... params) {
    super(messageKey.getKey(), statusCode, cause, params);
  }

  public ODataJPAFilterException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
    super(messageKey.getKey(), statusCode);
  }

  public ODataJPAFilterException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final String... params) {
    super(messageKey.getKey(), statusCode, params);
  }

  public ODataJPAFilterException(final MessageKeys messageKey, final HttpStatusCode statusCode, final Throwable e) {
    super(messageKey.getKey(), statusCode, e);
  }

  @Override
  protected String getBundleName() {
    return BUNDLE_NAME;
  }

}
