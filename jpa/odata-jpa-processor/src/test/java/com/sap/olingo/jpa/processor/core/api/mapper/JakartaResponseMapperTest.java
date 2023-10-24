package com.sap.olingo.jpa.processor.core.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.http.Cookie;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class JakartaResponseMapperTest {
  private static final String HEADER_VALUE = "HeaderValue";
  private static final String HEADER_NAME2 = "HeaderName2";
  private static final String HEADER_NAME = "HeaderName";
  private static final String COOKIE_PATH = "COOKIE_PATH";
  private static final String COOKIE_DOMAIN = "COOKIE_DOMAIN";
  private static final String COOKIE_COMMAND = "COOKIE_COMMAND";
  private static final String COOKIE_VALUE = "Watch You";
  private static final String COOKIE_NAME = "TestCookie";
  private HttpServletResponse jakartaResponse;
  private JakartaResponseMapper cut;

  @BeforeEach
  void setup() {
    jakartaResponse = mock(HttpServletResponse.class);
    cut = new JakartaResponseMapper(jakartaResponse);
  }

  @Test
  void testGetCharacterEncoding() {
    when(jakartaResponse.getCharacterEncoding()).thenReturn("UTF-8");
    assertEquals("UTF-8", cut.getCharacterEncoding());
  }

  @Test
  void testGetContentType() {
    when(jakartaResponse.getContentType()).thenReturn("HTML");
    assertEquals("HTML", cut.getContentType());
  }

  @Test
  void testGetOutputStream() throws IOException {
    final ServletOutputStream outputStream = mock(ServletOutputStream.class);
    when(jakartaResponse.getOutputStream()).thenReturn(outputStream);
    assertTrue(cut.getOutputStream() instanceof JakartaServletOutputStream);
  }

  @Test
  void testGetWriter() throws IOException {
    final PrintWriter writer = mock(PrintWriter.class);
    when(jakartaResponse.getWriter()).thenReturn(writer);
    assertEquals(writer, cut.getWriter());
  }

  @Test
  void testSetCharacterEncoding() throws UnsupportedEncodingException {
    cut.setCharacterEncoding("UTF-8");
    verify(jakartaResponse).setCharacterEncoding("UTF-8");
  }

  @Test
  void testSetContentLength() throws UnsupportedEncodingException {
    cut.setContentLength(654);
    verify(jakartaResponse).setContentLength(654);
  }

  @Test
  void testSetContentType() throws UnsupportedEncodingException {
    cut.setContentType("Test");
    verify(jakartaResponse).setContentType("Test");
  }

  @Test
  void testSetBufferSize() throws UnsupportedEncodingException {
    cut.setBufferSize(654);
    verify(jakartaResponse).setBufferSize(654);
  }

  @Test
  void testBufferSize() throws IOException {
    when(jakartaResponse.getBufferSize()).thenReturn(1000);
    assertEquals(1000, cut.getBufferSize());
  }

  @Test
  void testFlushBuffer() throws IOException {
    cut.flushBuffer();
    verify(jakartaResponse).flushBuffer();
  }

  @Test
  void testResetBuffer() throws IOException {
    cut.resetBuffer();
    verify(jakartaResponse).resetBuffer();
  }

  @Test
  void testIsCommitted() throws IOException {
    when(jakartaResponse.isCommitted()).thenReturn(true);
    assertEquals(true, cut.isCommitted());
  }

  @Test
  void testReset() throws IOException {
    cut.reset();
    verify(jakartaResponse).reset();
  }

  @Test
  void testSetLocale() throws UnsupportedEncodingException {
    cut.setLocale(Locale.CANADA_FRENCH);
    verify(jakartaResponse).setLocale(Locale.CANADA_FRENCH);
  }

  @Test
  void testGetLocale() {
    when(jakartaResponse.getLocale()).thenReturn(Locale.GERMANY);
    assertEquals(Locale.GERMANY, cut.getLocale());
  }

  @SuppressWarnings("removal")
  @Test
  void testAddCookies() {
    final Cookie cookie = buildCookie();
    cut.addCookie(cookie);

    final ArgumentCaptor<jakarta.servlet.http.Cookie> argument = ArgumentCaptor.forClass(
        jakarta.servlet.http.Cookie.class);
    verify(jakartaResponse).addCookie(argument.capture());

    final jakarta.servlet.http.Cookie act = argument.getValue();

    assertEquals(COOKIE_NAME, act.getName());
    assertEquals(COOKIE_VALUE, act.getValue());
    assertEquals(0, act.getVersion()); // See getter command
    assertNull(act.getComment()); // See getter command
    assertEquals(COOKIE_DOMAIN.toLowerCase(), act.getDomain());
    assertEquals(300000, act.getMaxAge());
    assertEquals(COOKIE_PATH, act.getPath());
    assertTrue(act.getSecure());
    assertTrue(act.isHttpOnly());
  }

  @Test
  void testContainsHeader() {
    when(jakartaResponse.containsHeader("Header")).thenReturn(true);
    assertTrue(cut.containsHeader("Header"));
    when(jakartaResponse.containsHeader("NoHeader")).thenReturn(false);
    assertFalse(cut.containsHeader("NoHeader"));
  }

  @Test
  void testEncodeURL() {
    when(jakartaResponse.encodeURL("test/test")).thenReturn("test/test");
    assertEquals("test/test", cut.encodeURL("test/test"));
  }

  @Test
  void testEncodeRedirectURL() {
    when(jakartaResponse.encodeRedirectURL("test/test")).thenReturn("test/test");
    assertEquals("test/test", cut.encodeRedirectURL("test/test"));
  }

  @Test
  void testEncodeUrl() {
    when(jakartaResponse.encodeURL("test/test")).thenReturn("test/test");
    assertEquals("test/test", cut.encodeUrl("test/test"));
  }

  @Test
  void testEncodeRedirectUrl() {
    when(jakartaResponse.encodeRedirectURL("test/test")).thenReturn("test/test");
    assertEquals("test/test", cut.encodeRedirectUrl("test/test"));
  }

  @Test
  void testSendError() throws IOException {
    cut.sendError(400);
    verify(jakartaResponse).sendError(400);
    cut.sendError(404, "Not Found");
    verify(jakartaResponse).sendError(404, "Not Found");
  }

  @Test
  void testSendRedirect() throws IOException {
    cut.sendRedirect("Redirect");
    verify(jakartaResponse).sendRedirect("Redirect");
  }

  @Test
  void testSetDateHeader() throws IOException {
    cut.setDateHeader(HEADER_NAME, 123);
    verify(jakartaResponse).setDateHeader(HEADER_NAME, 123);
  }

  @Test
  void testAddDateHeader() throws IOException {
    cut.addDateHeader(HEADER_NAME, 321);
    verify(jakartaResponse).addDateHeader(HEADER_NAME, 321);
  }

  @Test
  void testSetHeader() throws IOException {
    cut.setHeader(HEADER_NAME, "Hello");
    verify(jakartaResponse).setHeader(HEADER_NAME, "Hello");
  }

  @Test
  void testAddHeader() throws IOException {
    cut.addHeader(HEADER_NAME, "Test");
    verify(jakartaResponse).addHeader(HEADER_NAME, "Test");
  }

  @Test
  void testSetIntHeader() throws IOException {
    cut.setIntHeader(HEADER_NAME, 789);
    verify(jakartaResponse).setIntHeader(HEADER_NAME, 789);
  }

  @Test
  void testAddIntHeader() throws IOException {
    cut.addIntHeader(HEADER_NAME, 987);
    verify(jakartaResponse).addIntHeader(HEADER_NAME, 987);
  }

  @Test
  void testSetStatus() throws IOException {
    cut.setStatus(200);
    verify(jakartaResponse).setStatus(200);
  }

  @Test
  void testSetStatusMessage() throws IOException {
    cut.setStatus(201, "Created");
    verify(jakartaResponse).setStatus(201);
  }

  @Test
  void testGetStatus() {
    when(jakartaResponse.getStatus()).thenReturn(258);
    assertEquals(258, cut.getStatus());
  }

  @Test
  void testGetHeader() {
    when(jakartaResponse.getHeader(HEADER_NAME2)).thenReturn(HEADER_VALUE);
    assertEquals(HEADER_VALUE, cut.getHeader(HEADER_NAME2));
  }

  @Test
  void testGetHeaders() {
    @SuppressWarnings("unchecked")
    final Collection<String> collection = mock(Collection.class);
    when(jakartaResponse.getHeaders(HEADER_NAME)).thenReturn(collection);
    assertEquals(collection, cut.getHeaders(HEADER_NAME));
  }

  @Test
  void testGetHeaderNames() {
    @SuppressWarnings("unchecked")
    final Collection<String> collection = mock(Collection.class);
    when(jakartaResponse.getHeaderNames()).thenReturn(collection);
    assertEquals(collection, cut.getHeaderNames());
  }

  private Cookie buildCookie() {
    final Cookie cookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);
    cookie.setComment(COOKIE_COMMAND);
    cookie.setDomain(COOKIE_DOMAIN);
    cookie.setHttpOnly(true);
    cookie.setMaxAge(300000);
    cookie.setPath(COOKIE_PATH);
    cookie.setSecure(true);
    cookie.setVersion(3);
    return cookie;
  }
}
