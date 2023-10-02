package com.sap.olingo.jpa.metadata.core.edm.mapper.vocabularies;

import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import org.apache.olingo.commons.api.ex.ODataException;

public class ODataJPAVocabulariesException extends ODataException {

  private static final long serialVersionUID = 8367415692431546611L;
  private static final String BUNDLE_NAME = "vocabularies-exceptions-i18n";
  private static final String UNKNOWN_MESSAGE = "No message text found";

  private static final ResourceBundle BUNDLE = getResourceBundle();
  private final MessageKeys id;
  private final String[] parameters;

  public enum MessageKeys {
    FILE_NOT_FOUND,
    VARIABLE_NOT_SUPPORTED;

    public String getKey() {
      return name();
    }
  }

  public ODataJPAVocabulariesException(final MessageKeys id, final String... params) {
    super("");
    this.id = id;
    this.parameters = params;
  }

  public ODataJPAVocabulariesException(final MessageKeys id, final Exception cause, final String... params) {
    super("", cause);
    this.id = id;
    this.parameters = params;
  }

  private static ResourceBundle getResourceBundle() {
    return ResourceBundle.getBundle(BUNDLE_NAME, Locale.ENGLISH, Control.getNoFallbackControl(
        Control.FORMAT_PROPERTIES));
  }

  @Override
  public String getMessage() {
    if (id != null) {
      return getText();
    } else if (getCause() != null) {
      return getCause().getLocalizedMessage();
    } else
      return UNKNOWN_MESSAGE;
  }

  private String getText() {
    final String message = BUNDLE.getString(getClass().getSimpleName() + "." + id.getKey());
    final StringBuilder builder = new StringBuilder();
    final Formatter f = new Formatter(builder, Locale.ENGLISH);

    f.format(message, (Object[]) parameters);
    f.close();
    return builder.toString();
  }
}
