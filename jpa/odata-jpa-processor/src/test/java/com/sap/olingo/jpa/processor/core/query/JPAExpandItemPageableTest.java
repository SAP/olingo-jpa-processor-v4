package com.sap.olingo.jpa.processor.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.olingo.server.api.uri.queryoption.ExpandItem;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.processor.core.api.JPAODataExpandPage;
import com.sap.olingo.jpa.processor.core.api.JPAODataSkipTokenProvider;

abstract class JPAExpandItemPageableTest {

  protected ExpandItem expandItem;
  protected JPAEntityType et;

  abstract JPAExpandItemPageable getCut();

  JPAExpandItem asExpandItem() {
    return (JPAExpandItem) getCut();
  }

  @Test
  void testReturnsTopFromPage() {
    final var topOption = mock(TopOption.class);
    when(topOption.getValue()).thenReturn(10);
    when(expandItem.getTopOption()).thenReturn(topOption);

    final var page = new JPAODataExpandPage(asExpandItem(), 0, 5, null);
    getCut().setPage(page);
    assertEquals(5, asExpandItem().getTopOption().getValue());
  }

  @Test
  void testReturnsTopFromUriInfo() {
    final var topOption = mock(TopOption.class);
    when(topOption.getValue()).thenReturn(10);
    when(expandItem.getTopOption()).thenReturn(topOption);

    getCut().setPage(null);
    assertEquals(10, asExpandItem().getTopOption().getValue());
  }

  @Test
  void testReturnsSkipFromPage() {
    final var skipOption = mock(SkipOption.class);
    when(skipOption.getValue()).thenReturn(0);
    when(expandItem.getSkipOption()).thenReturn(skipOption);

    final var page = new JPAODataExpandPage(asExpandItem(), 10, 5, null);
    getCut().setPage(page);
    assertEquals(10, asExpandItem().getSkipOption().getValue());
  }

  @Test
  void testReturnsSkipFromUriInfo() {
    final var skipOption = mock(SkipOption.class);
    when(skipOption.getValue()).thenReturn(0);
    when(expandItem.getSkipOption()).thenReturn(skipOption);

    getCut().setPage(null);
    assertEquals(0, asExpandItem().getSkipOption().getValue());
  }

  @Test
  void testReturnsSkipTokenProviderFromPage() {
    final var provider = mock(JPAODataSkipTokenProvider.class);
    final var page = new JPAODataExpandPage(asExpandItem(), 10, 5, provider);
    getCut().setPage(page);
    assertEquals(provider, asExpandItem().getSkipTokenProvider().get());
  }

  @Test
  void testReturnsEmptySkipTokenProviderFromPage() {
    final var page = new JPAODataExpandPage(asExpandItem(), 10, 5, null);
    getCut().setPage(page);
    assertTrue(asExpandItem().getSkipTokenProvider().isEmpty());
  }

  @Test
  void testReturnsEmptySkipTokenProviderNoPage() {
    assertTrue(asExpandItem().getSkipTokenProvider().isEmpty());
  }

}
