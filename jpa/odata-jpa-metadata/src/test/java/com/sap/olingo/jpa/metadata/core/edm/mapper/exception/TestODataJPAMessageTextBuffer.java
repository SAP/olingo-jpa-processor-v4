package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.LocaleEnumeration;

class TestODataJPAMessageTextBuffer {
  private static String BUNDLE_NAME = "test-i18n";
  private ODataJPAMessageTextBuffer cut;

  @BeforeEach
  void setup() {
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME);
  }

  @Test
  void checkDefaultLocale() {
    assertEquals(ODataJPAMessageTextBuffer.DEFAULT_LOCALE.getLanguage(), cut.getLocale().getLanguage());
  }

  @Test
  void checkSetLocaleGerman() {
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, Locale.GERMANY);
    assertEquals("de", cut.getLocale().getLanguage());
  }

  @Test
  void checkSetLocaleReset() {
    // Set first to German
    checkSetLocaleGerman();
    // Then reset to default
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME);
    assertEquals(ODataJPAMessageTextBuffer.DEFAULT_LOCALE.getLanguage(), cut.getLocale().getLanguage());
  }

  @Test
  void checkGetDefaultLocaleText() {
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  void checkGetGermanText() {
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, Locale.GERMANY);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Ein deutscher Text", act);
  }

  // %1$s
  @Test
  void checkGetTextWithParameter() {
    final String act = cut.getText(this, "SECOND_MESSAGE", "Hugo", "Willi");
    assertEquals("Hugo looks for Willi", act);
  }

  @Test
  void checkSetLocalesNull() {
    final Enumeration<Locale> locales = null;
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, locales);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  void checkSetLocalesRestDefaultWithNull() {
    // First set to German
    checkSetLocaleGerman();
    // Then reset default
    final Enumeration<Locale> locales = null;
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, locales);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  void checkSetLocalesRestDefaultWithEmpty() {
    // First set to German
    checkSetLocaleGerman();
    // Then reset default
    final Enumeration<Locale> locales = new LocaleEnumeration(new ArrayList<Locale>());
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, locales);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }

  @Test
  void checkSetLocalesFirstMatches() {

    final ArrayList<Locale> localesList = new ArrayList<>();
    localesList.add(Locale.GERMAN);
    localesList.add(Locale.CANADA_FRENCH);
    final Enumeration<Locale> locales = new LocaleEnumeration(localesList);
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, locales);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Ein deutscher Text", act);
  }

  @Test
  void checkSetLocalesSecondMatches() {

    final ArrayList<Locale> localesList = new ArrayList<>();
    localesList.add(Locale.CANADA_FRENCH);
    localesList.add(Locale.GERMAN);
    final Enumeration<Locale> locales = new LocaleEnumeration(localesList);
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, locales);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("Ein deutscher Text", act);
  }

  @Test
  void checkSetLocalesNonMatches() {

    final ArrayList<Locale> localesList = new ArrayList<>();
    localesList.add(Locale.CANADA_FRENCH);
    localesList.add(Locale.SIMPLIFIED_CHINESE);
    final Enumeration<Locale> locales = new LocaleEnumeration(localesList);
    cut = new ODataJPAMessageTextBuffer(BUNDLE_NAME, locales);
    final String act = cut.getText(this, "FIRST_MESSAGE");
    assertEquals("An English message", act);
  }
}
