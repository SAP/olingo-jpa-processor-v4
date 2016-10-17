package org.apache.olingo.jpa.processor.core.exception;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageKey;

/*
 * Copied from org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException
 * See also org.apache.olingo.odata2.jpa.processor.core.exception.ODataJPAMessageServiceDefault
 */
public class ODataJPAProcessorException extends ODataJPAProcessException {
  /**
   * 
   */
  private static final long serialVersionUID = -7188499882306858747L;

  public static enum MessageKeys implements ODataJPAMessageKey {
    QUERY_PREPARATION_ERROR,
    QUERY_RESULT_CONV_ERROR,
    QUERY_RESULT_URI_ERROR,
    BATCH_CHANGE_SET_NOT_IMPLEMENTED,
    NOT_SUPPORTED_CREATE,
    NOT_SUPPORTED_UPDATE,
    NOT_SUPPORTED_DELETE,
    NOT_SUPPORTED_RESOURCE_TYPE,
    NOT_SUPPORTED_FUNC_WITH_NAVI,
    NOT_SUPPORTED_PROP_TYPE;

    @Override
    public String getKey() {
      return name();
    }

  }

  private static final String BUNDEL_NAME = "processor-exceptions-i18n";

  public ODataJPAProcessorException(final Throwable e, final HttpStatusCode statusCode) {
    super(e, statusCode);
  }

  public ODataJPAProcessorException(final MessageKeys messageKey, final HttpStatusCode statusCode,
      final Throwable cause, final String... params) {
    super(messageKey.getKey(), statusCode, cause, params);
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
    return BUNDEL_NAME;
  }

}
