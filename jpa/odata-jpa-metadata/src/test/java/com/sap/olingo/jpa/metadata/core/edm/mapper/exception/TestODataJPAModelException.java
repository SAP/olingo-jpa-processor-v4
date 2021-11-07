package com.sap.olingo.jpa.metadata.core.edm.mapper.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.testmodel.LocaleEnumeration;

class TestODataJPAModelException {
  private static String BUNDLE_NAME = "test-i18n";

  @Test
  void checkTextInDefaultLocale() {
    try {
      RaiseException();
    } catch (final ODataJPAException e) {
      assertEquals("An English message", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void checkTextInGerman() {
    try {
      final ArrayList<Locale> localesList = new ArrayList<>();
      localesList.add(Locale.GERMAN);
      final Enumeration<Locale> locales = new LocaleEnumeration(localesList);
      TestException.setLocales(locales);
      RaiseException();
    } catch (final ODataJPAException e) {
      assertEquals("Ein deutscher Text", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void checkTextInDefaultLocaleWithParameter() {
    try {
      RaiseExceptionParam();
    } catch (final ODataJPAException e) {
      assertEquals("Willi looks for Hugo", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void checkTextOnlyCause() {
    try {
      RaiseExceptionCause();
    } catch (final ODataJPAException e) {
      assertEquals("Test text from cause", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void checkTextIdAndCause() {
    try {
      RaiseExceptionIDCause();
    } catch (final ODataJPAException e) {
      assertEquals("An English message", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void checkTextIdAndCauseAndParameter() {
    try {
      RaiseExceptionIDCause("Willi", "Hugo");
    } catch (final ODataJPAException e) {
      assertEquals("Willi looks for Hugo", e.getMessage());
      return;
    }
    fail();
  }

  @Test
  void checkTextNullId() {
    try {
      RaiseEmptyIDException();
    } catch (final ODataJPAException e) {
      assertEquals("No message text found", e.getMessage());
      return;
    }
    fail();
  }

  private void RaiseExceptionIDCause(final String... params) throws TestException {
    try {
      raiseNullPointer();
    } catch (final NullPointerException e) {
      if (params.length == 0)
        throw new TestException("FIRST_MESSAGE", e);
      else
        throw new TestException("SECOND_MESSAGE", e, params);
    }
  }

  private void RaiseExceptionCause() throws ODataJPAException {
    try {
      raiseNullPointer();
    } catch (final NullPointerException e) {
      throw new TestException(e);
    }
  }

  private void raiseNullPointer() throws NullPointerException {
    throw new NullPointerException("Test text from cause");
  }

  private void RaiseExceptionParam() throws ODataJPAException {
    throw new TestException("SECOND_MESSAGE", "Willi", "Hugo");
  }

  private void RaiseException() throws ODataJPAException {
    throw new TestException("FIRST_MESSAGE");
  }

  private void RaiseEmptyIDException() throws ODataJPAException {
    throw new TestException("");
  }

  private class TestException extends ODataJPAException {

    private static final long serialVersionUID = 1L;

    public TestException(final String id) {
      super(id);
    }

    public TestException(final String id, final String... params) {
      super(id, params);
    }

    public TestException(final Throwable e) {
      super(e);
    }

    public TestException(final String id, final Throwable e) {
      super(id, e);
    }

    public TestException(final String id, final Throwable e, final String[] params) {
      super(id, e, params);
    }

    @Override
    protected String getBundleName() {
      return BUNDLE_NAME;
    }
  }
}
