package com.sap.olingo.jpa.metadata.core.edm.mapper.impl;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;

import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.api.JPAHttpHeaderMap;
import com.sap.olingo.jpa.metadata.api.JPARequestParameterMap;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmQueryExtensionProvider;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.testmodel.EmptyQueryExtensionProvider;

class JPAQueryExtensionProviderTest {

  @Test
  void checkReturnsDefaultConstructor() throws ODataJPAModelException {
    assertNotNull(new JPAQueryExtensionProvider<>(EmptyQueryExtensionProvider.class)
        .getConstructor());
  }

  @Test
  void checkThrowsExceptionTwoConstructors() {
    assertThrows(ODataJPAModelException.class, () -> new JPAQueryExtensionProvider<>(
        ExtensionProviderWithTwoConstructors.class).getConstructor());
  }

  @Test
  void checkThrowsExceptionWrongParameter() {
    assertThrows(ODataJPAModelException.class, () -> new JPAQueryExtensionProvider<>(
        ExtensionProviderWithWrongParameter.class).getConstructor());
  }

  @Test
  void checkReturnsConstructorWithHeaderAsParameter() throws ODataJPAModelException {
    assertNotNull(new JPAQueryExtensionProvider<>(ExtensionProviderWithAllowedParameter.class).getConstructor());
  }

  private static class ExtensionProviderWithTwoConstructors implements EdmQueryExtensionProvider {
    @SuppressWarnings("unused")
    private final String dummy;

    @Override
    public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {
      return null;
    }

    @SuppressWarnings("unused")
    public ExtensionProviderWithTwoConstructors(final String dummy) {
      this.dummy = dummy;
    }

    @SuppressWarnings("unused")
    public ExtensionProviderWithTwoConstructors() {
      this.dummy = "Hello";
    }
  }

  private static class ExtensionProviderWithAllowedParameter implements EdmQueryExtensionProvider {
    @SuppressWarnings("unused")
    private final Map<String, List<String>> header;
    @SuppressWarnings("unused")
    private final JPARequestParameterMap parameter;

    @Override
    public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {
      return null;
    }

    @SuppressWarnings("unused")
    public ExtensionProviderWithAllowedParameter(final JPAHttpHeaderMap header,
        final JPARequestParameterMap parameter) {
      this.header = requireNonNull(header);
      this.parameter = requireNonNull(parameter);
    }
  }

  private static class ExtensionProviderWithWrongParameter implements EdmQueryExtensionProvider {
    @SuppressWarnings("unused")
    private final String header;

    @Override
    public Expression<Boolean> getFilterExtension(final CriteriaBuilder cb, final From<?, ?> from) {
      return null;
    }

    @SuppressWarnings("unused")
    public ExtensionProviderWithWrongParameter(final String header) {
      this.header = header;
    }
  }
}
