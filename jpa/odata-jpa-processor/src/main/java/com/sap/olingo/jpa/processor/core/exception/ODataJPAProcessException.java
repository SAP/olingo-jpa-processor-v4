package com.sap.olingo.jpa.processor.core.exception;

import java.util.Enumeration;
import java.util.Locale;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageBufferRead;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageTextBuffer;

public abstract class ODataJPAProcessException extends ODataApplicationException {

  /**
   * 
   */
  private static final long serialVersionUID = -3178033271311091314L;
  private static final String UNKNOWN_MESSAGE = "No message text found";
  private static Enumeration<Locale> locales;

  protected final String id;
  protected final String[] parameter;
  protected final String messageText;

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode) {
    this(id, null, statusCode, new String[] {});
  }

  public ODataJPAProcessException(final Throwable cause, final HttpStatusCode statusCode) {
    this(null, null, statusCode, cause, new String[] {});
  }

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode, final Throwable cause) {
    this(id, null, statusCode, cause, new String[] {});
  }

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode, final Throwable cause,
      final String[] params) {
    this(id, null, statusCode, cause, params);
  }

  public ODataJPAProcessException(final String id, final HttpStatusCode statusCode, final String[] params) {
    this(id, null, statusCode, params);
  }

  /**
   * 
   * @param id
   * @param messageText
   * @param statusCode
   * @param params
   */
  public ODataJPAProcessException(final String id, final String messageText, final HttpStatusCode statusCode,
      final String[] params) {
    this(id, messageText, statusCode, null, params);
  }

  /**
   * 
   * @param id
   * @param messageText
   * @param statusCode
   * @param cause
   * @param params
   */
  public ODataJPAProcessException(final String id, final String messageText, final HttpStatusCode statusCode,
      final Throwable cause, final String[] params) {
    super("", statusCode.getStatusCode(), Locale.ENGLISH, cause);
    this.id = id;
    this.parameter = params;
    this.messageText = messageText;
  }

  protected ODataJPAMessageTextBuffer getTextBundle() {
    if (getBundleName() != null)
      return new ODataJPAMessageTextBuffer(getBundleName(), locales);
    else
      return null;
  }

  @Override
  public String getLocalizedMessage() {
    return getMessage();
  }

  @Override
  public String getMessage() {
    ODataJPAMessageBufferRead messageBuffer = getTextBundle();

    if (messageBuffer != null && id != null) {
      String message = messageBuffer.getText(this, id, parameter);
      if (message != null) {
        return message;
      }
      return messageText;
    } else if (getCause() != null) {
      return getCause().getLocalizedMessage();
    } else if (messageText != null && !messageText.isEmpty())
      return messageText;
    else
      return UNKNOWN_MESSAGE;
  }

  public String[] getParameter() {
    return parameter;
  }

  public String getId() {
    return id;
  }

  public static Enumeration<Locale> getLocales() {
    return locales;
  }

  public static void setLocales(final Enumeration<Locale> locales) {
    ODataJPAProcessException.locales = locales;
  }

  protected abstract String getBundleName();
}
