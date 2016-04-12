package org.apache.olingo.jpa.metadata.core.edm.mapper.exception;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import org.apache.olingo.jpa.processor.core.testmodel.LocaleEnumeration;
import org.junit.Before;
import org.junit.Test;

public class TestODataJPAMessageTextBuffer {
  private static String BUNDLE_NAME = "test-i18n";
  private ODataJPAMessageTextBuffer cut;

  @Before
  public void setup() {
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME);
  }

  @Test
  public void checkDefaultLocale() {
    assertEquals(ODataJPAMessageTextBuffer.DEFAULT_LOCALE.getLanguage(), cut.getLocale().getLanguage());
  }

  @Test
  public void checkSetLocaleGerman() {
    cut.setLocale(Locale.GERMANY);
    assertEquals("de", cut.getLocale().getLanguage());
  }

  @Test
  public void checkSetLocaleReset() {
    // Set first to German
    checkSetLocaleGerman();
    // Then reset to default
    cut.setLocale(null);
    assertEquals(ODataJPAMessageTextBuffer.DEFAULT_LOCALE.getLanguage(), cut.getLocale().getLanguage());
  }

  @Test
  public void checkGetDefaultLocaleText() {
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  public void checkGetGermanText() {
    cut.setLocale(Locale.GERMAN);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Ein deutscher Text", act);
  }

  @Test
  public void checkGetOtherBundle() {
    cut.setBundleName("test-i18n2");
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Another English message", act);
  }

  // %1$s
  @Test
  public void checkGetTextWithParameter() {
    String act = cut.getText(this, "SECOND_MESSAGE", "Hugo", "Willi");
    assertEquals("Hugo looks for Willi", act);
  }

  @Test
  public void checkSetLocalesNull() {
    Enumeration<Locale> locales = null;
    cut.setLocales(locales);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  public void checkSetLocalesRestDefaultWithNull() {
    // First set to German
    checkSetLocaleGerman();
    // Then reset default
    Enumeration<Locale> locales = null;
    cut.setLocales(locales);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  public void checkSetLocalesRestDefaultWithEmpty() {
    // First set to German
    checkSetLocaleGerman();
    // Then reset default
    Enumeration<Locale> locales = new LocaleEnumeration(new ArrayList<Locale>());
    cut.setLocales(locales);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  public void checkSetLocalesFirstMatches() {

    ArrayList<Locale> localesList = new ArrayList<Locale>();
    localesList.add(Locale.GERMAN);
    localesList.add(Locale.CANADA_FRENCH);
    Enumeration<Locale> locales = new LocaleEnumeration(localesList);
    cut.setLocales(locales);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Ein deutscher Text", act);
  }

  @Test
  public void checkSetLocalesSecondMatches() {

    ArrayList<Locale> localesList = new ArrayList<Locale>();
    localesList.add(Locale.CANADA_FRENCH);
    localesList.add(Locale.GERMAN);
    Enumeration<Locale> locales = new LocaleEnumeration(localesList);
    cut.setLocales(locales);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Ein deutscher Text", act);
  }

  @Test
  public void checkSetLocalesNonMatches() {

    ArrayList<Locale> localesList = new ArrayList<Locale>();
    localesList.add(Locale.CANADA_FRENCH);
    localesList.add(Locale.SIMPLIFIED_CHINESE);
    Enumeration<Locale> locales = new LocaleEnumeration(localesList);
    cut.setLocales(locales);
    String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }
}
