package com.sap.olingo.jpa.processor.core.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.TopOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;

class JPADefaultPagingProviderTest {

  private JPAODataPagingProvider cut;
  private UriInfo uriInfo;
  private SkipOption skipOption;
  private TopOption topOption;

  @BeforeEach
  void setup() {
    cut = new JPADefaultPagingProvider();
    uriInfo = mock(UriInfo.class);
    skipOption = mock(SkipOption.class);
    topOption = mock(TopOption.class);
  }

  @Test
  void testGetNextPageReturnsEmptyOptional() {
    assertTrue(cut.getNextPage("Test", null, null, null, null).isEmpty());
  }

  @Test
  void testGetFirstPageReturnsSkipZeroIfAbsence() throws ODataApplicationException {
    when(uriInfo.getSkipOption()).thenReturn(null);
    final var act = cut.getFirstPage(null, null, uriInfo, null, null, null);
    assertEquals(0, act.orElseGet(() -> fail("No page found")).skip());
  }

  @Test
  void testGetFirstPageReturnsSkipAsRequested() throws ODataApplicationException {
    when(skipOption.getValue()).thenReturn(99);
    when(uriInfo.getSkipOption()).thenReturn(skipOption);
    final var act = cut.getFirstPage(null, null, uriInfo, null, null, null);
    assertEquals(99, act.orElseGet(() -> fail("No page found")).skip());
  }

  @Test
  void testGetFirstPageReturnsTopMaxIfAbsence() throws ODataApplicationException {
    when(uriInfo.getTopOption()).thenReturn(null);
    final var act = cut.getFirstPage(null, null, uriInfo, null, null, null);
    assertEquals(Integer.MAX_VALUE, act.orElseGet(() -> fail("No page found")).top());
  }

  @Test
  void testGetFirstPageReturnsTopAsRequested() throws ODataApplicationException {
    when(topOption.getValue()).thenReturn(99);
    when(uriInfo.getTopOption()).thenReturn(topOption);
    final var act = cut.getFirstPage(null, null, uriInfo, null, null, null);
    assertEquals(99, act.orElseGet(() -> fail("No page found")).top());
  }

  @Test
  void testGetFirstPageReturnsTopAndSkipAsRequested() throws ODataApplicationException {
    when(topOption.getValue()).thenReturn(13);
    when(uriInfo.getTopOption()).thenReturn(topOption);
    when(skipOption.getValue()).thenReturn(99);
    when(uriInfo.getSkipOption()).thenReturn(skipOption);
    final var act = cut.getFirstPage(null, null, uriInfo, null, null, null);
    assertEquals(13, act.orElseGet(() -> fail("No page found")).top());
    assertEquals(99, act.orElseGet(() -> fail("No page found")).skip());
    assertNull(act.get().skipToken());
  }

  @Test
  void testGetFirstPageThrowsExceptionSkipNegative() throws ODataApplicationException {
    when(skipOption.getValue()).thenReturn(-99);
    when(uriInfo.getSkipOption()).thenReturn(skipOption);
    assertThrows(ODataJPAQueryException.class, () -> cut.getFirstPage(null, null, uriInfo, null, null, null));
  }

  @Test
  void testGetFirstPageThrowsExceptionTopNegative() throws ODataApplicationException {
    when(topOption.getValue()).thenReturn(-99);
    when(uriInfo.getTopOption()).thenReturn(topOption);
    assertThrows(ODataJPAQueryException.class, () -> cut.getFirstPage(null, null, uriInfo, null, null, null));
  }
}
