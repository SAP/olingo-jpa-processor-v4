package org.apache.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAQueryException extends ODataJPAProcessException {
  /**
   * 
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public static enum MessageKeys implements ODataJPAMessageKey {
    QUERY_RESULT_CONV_ERROR,
    QUERY_RESULT_ENTITY_SET_ERROR,
    QUERY_RESULT_NAVI_PROPERTY_ERROR,
    QUERY_RESULT_KEY_PROPERTY_ERROR,
    QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
    QUERY_RESULT_ACCESS_NOT_FOUND,
    QUERY_RESULT_EXPAND_ERROR,
    QUERY_PREPARATION_FILTER_ERROR,
    QUERY_PREPARATION_ENTITY_UNKNOWN,
    QUERY_PREPARATION_INVALID_VALUE,
    QUERY_PREPARATION_ERROR,
    NOT_SUPPORTED_RESOURCE_TYPE;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDEL_NAME = "exceptions-i18n";

  public ODataJPAQueryException(Throwable e, final HttpStatusCode statusCode) {
    super(e, statusCode);
  }

  public ODataJPAQueryException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final Throwable cause, final String... params) {
    super(messageKey.getKey(), statusCode, cause, params);
  }

  public ODataJPAQueryException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
    super(messageKey.getKey(), statusCode);
  }

  public ODataJPAQueryException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final String... params) {
    super(messageKey.getKey(), statusCode, params);
  }

  public ODataJPAQueryException(final MessageKeys messageKey, final HttpStatusCode statusCode, final Throwable e) {
    super(messageKey.getKey(), statusCode, e);
  }

  @Override
  protected String getBundleName() {
    return BUNDEL_NAME;
  }

}
