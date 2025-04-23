package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.olingo.server.api.uri.UriResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JPAExpandItemTest {

  private JPAExpandItem cut;

  @BeforeEach
  void setup() {
    cut = new JPAExpandItemImpl();
  }

  @Test
  void testDefaultReturnsNull() {
    assertNull(cut.getApplyOption());
    assertNull(cut.getCountOption());
    assertNull(cut.getDeltaTokenOption());
    assertNull(cut.getExpandOption());
    assertNull(cut.getFilterOption());
    assertNull(cut.getFormatOption());
    assertNull(cut.getIdOption());
    assertNull(cut.getOrderByOption());
    assertNull(cut.getSearchOption());
    assertNull(cut.getSkipOption());
    assertNull(cut.getSkipTokenOption());
    assertNull(cut.getTopOption());
    assertNull(cut.getEntityType());
  }

  @Test
  void testDefaultReturnsEmptyList() {
    assertTrue(cut.getCustomQueryOptions().isEmpty());
  }

  @Test
  void testDefaultReturnsEmptyOption() {
    assertTrue(cut.getSkipTokenProvider().isEmpty());
  }

  private static class JPAExpandItemImpl implements JPAExpandItem {

    @Override
    public List<UriResource> getUriResourceParts() {
      return List.of();
    }

    @Override
    public String getValueForAlias(final String alias) {
      return null;
    }

  }

}
