package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

import java.util.Enumeration;
import java.util.Formatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class ODataJPAMessageTextBuffer implements ODataJPAMessageBufferRead {
  private static final String PATH_SEPARATOR = ".";

  public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  protected final String bundleName;
  protected final ResourceBundle bundle;
  protected final Locale locale;

  public ODataJPAMessageTextBuffer(final String bundleName) {
    super();
    this.bundleName = bundleName;
    this.locale = DEFAULT_LOCALE;
    this.bundle = getResourceBundle(locale);
  }

  public ODataJPAMessageTextBuffer(final String bundleName, final Enumeration<Locale> locales) {
    super();
    this.bundleName = bundleName;
    this.locale = setLocales(locales);
    this.bundle = getResourceBundle(locale);
  }

  public ODataJPAMessageTextBuffer(final String bundleName, final Locale locale) {
    this.bundleName = bundleName;
    this.locale = locale;
    this.bundle = getResourceBundle(locale);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAMessageBufferRead#getText(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public String getText(final Object execution, final String id) {
    return bundle.getString(execution.getClass().getSimpleName() + PATH_SEPARATOR + id);
  }

  @Override
  public String getText(final Object execution, final String ID, final String... parameters) {
    final String message = getText(execution, ID);
    final StringBuilder builder = new StringBuilder();
    final Formatter f = new Formatter(builder, locale);

    f.format(message, (Object[]) parameters);
    f.close();
    return builder.toString();
  }

  private Locale setLocales(final Enumeration<Locale> locales) {
    ResourceBundle resourceBundle;
    Locale resourceLocale = DEFAULT_LOCALE;
    if (locales != null && locales.hasMoreElements()) {
      while (locales.hasMoreElements()) {
        resourceLocale = locales.nextElement();
        resourceBundle = getResourceBundle(resourceLocale);
        if (resourceBundle.getLocale().getLanguage().equals(resourceLocale.getLanguage())
            && resourceBundle.getLocale().getCountry().equals(resourceLocale.getCountry()))
          break;
      }
    }
    return resourceLocale;
  }

  String getBundleName() {
    return bundleName;
  }

  Locale getLocale() {
    return locale;
  }

  private ResourceBundle getResourceBundle(final Locale resourceLocale) {
    return ResourceBundle.getBundle(bundleName, resourceLocale, Control.getNoFallbackControl(
        Control.FORMAT_PROPERTIES));
  }
}
