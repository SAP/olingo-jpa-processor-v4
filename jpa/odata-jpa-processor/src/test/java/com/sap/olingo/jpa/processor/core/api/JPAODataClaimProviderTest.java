package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class JPAODataClaimProviderTest {

  @Test
  void testDefaultImplementationsReturnsEmpty() {
    final JPAODataClaimProvider cut = new DummyImpl();
    final Optional<String> act = cut.user();
    assertFalse(act.isPresent());
  }

  private static class DummyImpl implements JPAODataClaimProvider {

    @Override
    public List<JPAClaimsPair<?>> get(final String attributeName) {
      return null;
    }
  }
}
