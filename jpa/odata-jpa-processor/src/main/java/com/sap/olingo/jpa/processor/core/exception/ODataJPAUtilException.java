package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAUtilException extends ODataJPAProcessException {
  /**
   * 
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public enum MessageKeys implements ODataJPAMessageKey {
    UNKNOWN_NAVI_PROPERTY,
    UNKNOWN_ENTITY_TYPE;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDEL_NAME = "processor-exceptions-i18n";

  public ODataJPAUtilException(final Throwable e, final HttpStatusCode statusCode) {
    super(e, statusCode);
  }

  public ODataJPAUtilException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final Throwable cause, final String... params) {
    super(messageKey.getKey(), statusCode, cause, params);
  }

  public ODataJPAUtilException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
    super(messageKey.getKey(), statusCode);
  }

  public ODataJPAUtilException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final String... params) {
    super(messageKey.getKey(), statusCode, params);
  }

  public ODataJPAUtilException(final MessageKeys messageKey, final HttpStatusCode statusCode, final Throwable e) {
    super(messageKey.getKey(), statusCode, e);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

}
