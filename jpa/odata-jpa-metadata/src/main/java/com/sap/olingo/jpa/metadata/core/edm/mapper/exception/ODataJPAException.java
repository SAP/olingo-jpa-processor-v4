package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

import java.util.Enumeration;
import java.util.Locale;

import org.apache.olingo.commons.api.ex.ODataException;

public abstract class ODataJPAException extends ODataException {

  private static final long serialVersionUID = 1148357369597923853L;
  private static final String UNKNOWN_MESSAGE = "No message text found";
  private static Enumeration<Locale> locales;

  public static Enumeration<Locale> getLocales() {
    return locales;
  }

  public static void setLocales(final Enumeration<Locale> locales) {
    ODataJPAException.locales = locales;
  }

  protected final String id;
  protected final ODataJPAMessageTextBuffer messageBuffer;
  protected final String[] parameter;

  protected ODataJPAException(final String id) {
    super("");
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName(), locales);
    this.parameter = null;
  }

  protected ODataJPAException(final String id, final String... params) {
    super("");
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName(), locales);
    this.parameter = params;
  }

  protected ODataJPAException(final String id, final Throwable cause, final String... params) {
    super("", cause);
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName(), locales);
    this.parameter = params;
  }

  protected ODataJPAException(final String id, final Throwable cause) {
    super("", cause);
    this.id = id;
    this.messageBuffer = new ODataJPAMessageTextBuffer(getBundleName(), locales);
    this.parameter = null;
  }

  protected ODataJPAException(final Throwable cause) {
    super(cause);
    id = null;
    messageBuffer = null;
    this.parameter = null;
  }

  @Override
  public String getLocalizedMessage() {
    return getMessage();
  }

  @Override
  public String getMessage() {
    if (id != null && !id.isEmpty() && messageBuffer != null) {
      return messageBuffer.getText(this, id, parameter);
    } else if (getCause() != null) {
      return getCause().getLocalizedMessage();
    } else
      return UNKNOWN_MESSAGE;
  }

  protected abstract String getBundleName();

}
