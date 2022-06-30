package com.sap.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAProcessorException extends ODataJPAProcessException { // NOSONAR
  /**
   *
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public enum MessageKeys implements ODataJPAMessageKey {
    QUERY_PREPARATION_ERROR,
    QUERY_RESULT_CONV_ERROR,
    QUERY_RESULT_URI_ERROR,
    QUERY_SERVER_DRIVEN_PAGING_NOT_IMPLEMENTED,
    QUERY_SERVER_DRIVEN_PAGING_GONE,
    BATCH_CHANGE_SET_NOT_IMPLEMENTED,
    NOT_SUPPORTED_CREATE,
    NOT_SUPPORTED_UPDATE,
    NOT_SUPPORTED_UPDATE_VALUE,
    NOT_SUPPORTED_DELETE,
    NOT_SUPPORTED_DELETE_VALUE,
    NOT_SUPPORTED_RESOURCE_TYPE,
    NOT_SUPPORTED_FUNC_WITH_NAVI,
    NOT_SUPPORTED_PROP_TYPE,
    NOT_SUPPORTED_COUNT,
    PARAMETER_NULL,
    WRONG_RETURN_TYPE,
    RETURN_NULL,
    RETURN_MISSING_ENTITY,
    ATTRIBUTE_RETRIEVAL_FAILED,
    ATTRIBUTE_NOT_FOUND,
    ODATA_MAXPAGESIZE_NOT_A_NUMBER,
    SETTER_NOT_FOUND,
    GETTER_NOT_FOUND,
    NO_COLLECTION_RETURNED_BY_GETTER,
    BEFORE_IMAGE_MERGED,
    ENTITY_TYPE_UNKNOWN,
    FUNCTION_UNKNOWN,
    ACTION_UNKNOWN,
    ENUMERATION_UNKNOWN,
    NO_METADATA_PROVIDER;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDLE_NAME = "processor-exceptions-i18n";

  public ODataJPAProcessorException(final Throwable e, final HttpStatusCode statusCode) {
    super(e, statusCode);
  }

  public ODataJPAProcessorException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final Throwable cause, final String... params) {
    super(messageKey.getKey(), statusCode, cause, params);
  }

  public ODataJPAProcessorException(final String messageKey, final HttpStatusCode statusCode,
      final Throwable cause, final String[] params) {
    super(messageKey, statusCode, cause, params);
  }

  public ODataJPAProcessorException(final MessageKeys messageKey, final HttpStatusCode statusCode) {
    super(messageKey.getKey(), statusCode);
  }

  public ODataJPAProcessorException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final String... params) {
    super(messageKey.getKey(), statusCode, params);
  }

  public ODataJPAProcessorException(final MessageKeys messageKey, final HttpStatusCode statusCode, final Throwable e) {
    super(messageKey.getKey(), statusCode, e);
  }

  @Override
  protected String getBundleName() {
    return BUNDLE_NAME;
  }

}
