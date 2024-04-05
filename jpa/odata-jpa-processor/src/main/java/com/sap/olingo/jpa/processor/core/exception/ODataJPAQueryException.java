package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAQueryException extends ODataJPAProcessException { // NOSONAR
  /**
   *
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public enum MessageKeys implements ODataJPAMessageKey {
    QUERY_RESULT_CONV_ERROR,
    QUERY_RESULT_ENTITY_SET_ERROR,
    QUERY_RESULT_ENTITY_TYPE_ERROR,
    QUERY_RESULT_NAVI_PROPERTY_ERROR,
    QUERY_RESULT_KEY_PROPERTY_ERROR,
    QUERY_RESULT_NAVI_PROPERTY_UNKNOWN,
    QUERY_RESULT_ACCESS_NOT_FOUND,
    QUERY_RESULT_EXPAND_ERROR,
    QUERY_RESULT_CONV_MISSING_GETTER,
    QUERY_PREPARATION_FILTER_ERROR,
    QUERY_PREPARATION_ENTITY_UNKNOWN,
    QUERY_PREPARATION_INVALID_VALUE,
    QUERY_PREPARATION_INVALID_SELECTION_PATH,
    QUERY_PREPARATION_INVALID_KEY_PAIR,
    QUERY_PREPARATION_ERROR,
    QUERY_PREPARATION_JOIN_NOT_DEFINED,
    QUERY_PREPARATION_NOT_IMPLEMENTED,
    QUERY_PREPARATION_NOT_ALLOWED_MEMBER,
    QUERY_PREPARATION_ORDER_BY_TRANSIENT,
    QUERY_PREPARATION_ORDER_BY_NOT_SUPPORTED,
    QUERY_PREPARATION_JOIN_TABLE_TYPE_MISSING,
    QUERY_PREPARATION_COLLECTION_PROPERTY_NOT_SUPPORTED,
    NOT_SUPPORTED_RESOURCE_TYPE,
    MISSING_CLAIMS_PROVIDER,
    MISSING_CLAIM,
    WILDCARD_UPPER_NOT_SUPPORTED,

    QUERY_CHECK_SORTING_NOT_SUPPORTED,
    QUERY_CHECK_SORTING_NOT_SUPPORTED_FOR,
    QUERY_CHECK_ASCENDING_REQUIRED_FOR,
    QUERY_CHECK_DESCENDING_REQUIRED_FOR;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDLE_NAME = "processor-exceptions-i18n";

  public ODataJPAQueryException(final Throwable e, final HttpStatusCode statusCode) {
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
    return BUNDLE_NAME;
  }

}
