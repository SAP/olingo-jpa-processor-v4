package com.sap.olingo.jpa.processor.core.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JakartaRequestMapperTest {
  private static final String PARAMETER_NAME = "Parameter Name";
  private static final String PATH = "test/path";
  private static final String HEADER_VALUE = "Header Value";
  private static final String DATE_HEADER = "DateHeader";
  private static final String COOKIE_PATH = "COOKIE_PATH";
  private static final String COOKIE_DOMAIN = "COOKIE_DOMAIN";
  private static final String COOKIE_COMMAND = "COOKIE_COMMAND";
  private static final String COOKIE_ATTRIBUTE_VALUE = "COOKIE_ATTRIBUTE_VALUE";
  private static final String COOKIE_ATTRIBUTE = "COOKIE_ATTRIBUTE";
  private static final String COOKIE_VALUE = "Watch You";
  private static final String COOKIE_NAME = "TestCookie";
  private static final String HEADER_NAME = "SingleHeader";
  private JakartaRequestMapper cut;
  private HttpServletRequest jakartaRequest;

  @BeforeEach
  void setup() {
    jakartaRequest = mock(HttpServletRequest.class);
    cut = new JakartaRequestMapper(jakartaRequest);
  }

  @Test
  void testJakartaRequestMapperThrowsNPE() {
    assertThrows(NullPointerException.class, () -> new JakartaRequestMapper(null));
  }

  @Test
  void testGetAuthType() {
    when(jakartaRequest.getAuthType()).thenReturn("Basic");
    assertEquals("Basic", cut.getAuthType());
  }

  @Test
  void testGetCookies() {
    final Cookie cookie = buildCookie();
    when(jakartaRequest.getCookies()).thenReturn(new Cookie[] { cookie });
    final javax.servlet.http.Cookie[] acts = cut.getCookies();
    assertEquals(1, acts.length);
    final javax.servlet.http.Cookie act = acts[0];
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
  void testGetDateHeader() {
    when(jakartaRequest.getDateHeader(DATE_HEADER)).thenReturn(10L);
    assertEquals(10L, cut.getDateHeader(DATE_HEADER));
  }

  @Test
  void testGetHeader() {
    when(jakartaRequest.getHeader(HEADER_NAME)).thenReturn(HEADER_VALUE);
    assertEquals(HEADER_VALUE, cut.getHeader(HEADER_NAME));
  }

  @Test
  void testGetHeaders() {
    @SuppressWarnings("unchecked")
    final Enumeration<String> enumeration = mock(Enumeration.class);
    when(jakartaRequest.getHeaders(HEADER_NAME)).thenReturn(enumeration);
    assertEquals(enumeration, cut.getHeaders(HEADER_NAME));
  }

  @Test
  void testGetHeaderNames() {
    @SuppressWarnings("unchecked")
    final Enumeration<String> enumeration = mock(Enumeration.class);
    when(jakartaRequest.getHeaderNames()).thenReturn(enumeration);
    assertEquals(enumeration, cut.getHeaderNames());
  }

  @Test
  void testGetIntHeader() {
    when(jakartaRequest.getIntHeader(HEADER_NAME)).thenReturn(10);
    assertEquals(10, cut.getIntHeader(HEADER_NAME));
  }

  @Test
  void testGetMethod() {
    when(jakartaRequest.getMethod()).thenReturn("POST");
    assertEquals("POST", cut.getMethod());
  }

  @Test
  void testGetPathInfo() {
    when(jakartaRequest.getPathInfo()).thenReturn(PATH);
    assertEquals(PATH, cut.getPathInfo());
  }

  @Test
  void testGetPathTranslated() {
    when(jakartaRequest.getPathTranslated()).thenReturn(PATH);
    assertEquals(PATH, cut.getPathTranslated());
  }

  @Test
  void testGetContextPath() {
    when(jakartaRequest.getContextPath()).thenReturn(PATH);
    assertEquals(PATH, cut.getContextPath());
  }

  @Test
  void testGetQueryString() {
    when(jakartaRequest.getQueryString()).thenReturn(PATH);
    assertEquals(PATH, cut.getQueryString());
  }

  @Test
  void testGetRemoteUser() {
    when(jakartaRequest.getRemoteUser()).thenReturn("Willi");
    assertEquals("Willi", cut.getRemoteUser());
  }

  @Test
  void testIsUserInRole() {
    when(jakartaRequest.isUserInRole("Manager")).thenReturn(true);
    when(jakartaRequest.isUserInRole("Employee")).thenReturn(false);
    assertTrue(cut.isUserInRole("Manager"));
    assertFalse(cut.isUserInRole("Employee"));
  }

  @Test
  void testGetUserPrincipal() {
    final Principal principal = mock(Principal.class);
    when(jakartaRequest.getUserPrincipal()).thenReturn(principal);
    assertEquals(principal, cut.getUserPrincipal());
  }

  @Test
  void testGetRequestedSessionId() {
    when(jakartaRequest.getRequestedSessionId()).thenReturn("123");
    assertNull(cut.getRequestedSessionId());
  }

  @Test
  void testGetRequestURI() {
    when(jakartaRequest.getRequestURI()).thenReturn("/test/hallo.html");
    assertEquals("/test/hallo.html", cut.getRequestURI());
  }

  @Test
  void testGetRequestURL() {
    final StringBuffer url = new StringBuffer();
    when(jakartaRequest.getRequestURL()).thenReturn(url);
    assertEquals(url, cut.getRequestURL());
  }

  @Test
  void testGetServletPath() {
    when(jakartaRequest.getServletPath()).thenReturn(PATH + HEADER_VALUE);
    assertEquals(PATH + HEADER_VALUE, cut.getServletPath());
  }

  @Test
  void testGetSessionThrowsException() {
    assertThrows(IllegalAccessError.class, () -> cut.getSession(true));
    assertThrows(IllegalAccessError.class, () -> cut.getSession());
  }

  @Test
  void testIsRequestedSessionIdValid() {
    when(jakartaRequest.isRequestedSessionIdValid()).thenReturn(true);
    assertTrue(cut.isRequestedSessionIdValid());
  }

  @Test
  void testIsRequestedSessionIdFromCookie() {
    when(jakartaRequest.isRequestedSessionIdFromCookie()).thenReturn(true);
    assertTrue(cut.isRequestedSessionIdFromCookie());
  }

  @Test
  void testIsRequestedSessionIdFromURL() {
    when(jakartaRequest.isRequestedSessionIdFromURL()).thenReturn(true);
    assertTrue(cut.isRequestedSessionIdFromURL());
  }

  @Test
  void testIsRequestedSessionIdFromUrl() {
    when(jakartaRequest.isRequestedSessionIdFromURL()).thenReturn(true);
    assertTrue(cut.isRequestedSessionIdFromUrl());
  }

  @Test
  void testAuthenticate() throws IOException, ServletException, javax.servlet.ServletException {
    final HttpServletResponse response = mock(HttpServletResponse.class);
    final javax.servlet.http.HttpServletResponse responseOld = mock(javax.servlet.http.HttpServletResponse.class);

    when(jakartaRequest.authenticate(response)).thenReturn(true);
    assertFalse(cut.authenticate(responseOld));
  }

  @Test
  void testLogin() throws javax.servlet.ServletException, ServletException {
    cut.login("Willi", "1234");
    verify(jakartaRequest).login("Willi", "1234");
  }

  @Test
  void testLoginRethrowsException() throws javax.servlet.ServletException, ServletException {
    doThrow(new ServletException()).when(jakartaRequest).login("Willi", "1234");
    assertThrows(javax.servlet.ServletException.class, () -> cut.login("Willi", "1234"));
  }

  @Test
  void testLogout() throws javax.servlet.ServletException, ServletException {
    cut.logout();
    verify(jakartaRequest).logout();
  }

  @Test
  void testLogoutRethrowsException() throws javax.servlet.ServletException, ServletException {
    doThrow(new ServletException()).when(jakartaRequest).logout();
    assertThrows(javax.servlet.ServletException.class, () -> cut.logout());
  }

  @Test
  void testGetPartThrowsException() {
    assertThrows(IllegalAccessError.class, () -> cut.getPart(COOKIE_NAME));
    assertThrows(IllegalAccessError.class, () -> cut.getParts());
  }

  @Test
  void testGetAttributeNames() {
    @SuppressWarnings("unchecked")
    final Enumeration<String> enumeration = mock(Enumeration.class);
    when(jakartaRequest.getAttributeNames()).thenReturn(enumeration);
    assertEquals(enumeration, cut.getAttributeNames());
  }

  @Test
  void testGetCharacterEncoding() {
    when(jakartaRequest.getCharacterEncoding()).thenReturn("UTF-8");
    assertEquals("UTF-8", cut.getCharacterEncoding());
  }

  @Test
  void testSetCharacterEncoding() throws UnsupportedEncodingException {
    cut.setCharacterEncoding("UTF-8");
    verify(jakartaRequest).setCharacterEncoding("UTF-8");
  }

  @Test
  void testGetContentLength() {
    when(jakartaRequest.getContentLength()).thenReturn(356);
    assertEquals(356, cut.getContentLength());
  }

  @Test
  void testGetContentType() {
    when(jakartaRequest.getContentType()).thenReturn("HTML");
    assertEquals("HTML", cut.getContentType());
  }

  @Test
  void testGetInputStream() throws IOException {
    final ServletInputStream inputStream = mock(ServletInputStream.class);
    when(jakartaRequest.getInputStream()).thenReturn(inputStream);
    assertTrue(cut.getInputStream() instanceof JakartaServletInputStream);
  }

  @Test
  void testGetParameter() throws UnsupportedEncodingException {
    when(jakartaRequest.getParameter(PARAMETER_NAME)).thenReturn("HTML");
    assertEquals("HTML", cut.getParameter(PARAMETER_NAME));
  }

  @Test
  void testGetParameterNames() throws UnsupportedEncodingException {
    @SuppressWarnings("unchecked")
    final Enumeration<String> enumeration = mock(Enumeration.class);
    when(jakartaRequest.getParameterNames()).thenReturn(enumeration);
    assertEquals(enumeration, cut.getParameterNames());
  }

  @Test
  void testGetParameterValues() throws UnsupportedEncodingException {
    final String[] values = new String[] { "A", "B" };
    when(jakartaRequest.getParameterValues(PARAMETER_NAME)).thenReturn(values);
    assertEquals(values, cut.getParameterValues(PARAMETER_NAME));
  }

  @Test
  void testGetParameterMap() throws UnsupportedEncodingException {
    final Map<String, String[]> parameters = new HashMap<>();
    when(jakartaRequest.getParameterMap()).thenReturn(parameters);
    assertEquals(parameters, cut.getParameterMap());
  }

  @Test
  void testGetScheme() throws UnsupportedEncodingException {
    when(jakartaRequest.getScheme()).thenReturn("http");
    assertEquals("http", cut.getScheme());
  }

  @Test
  void testGetServerName() throws UnsupportedEncodingException {
    when(jakartaRequest.getServerName()).thenReturn("localhost");
    assertEquals("localhost", cut.getServerName());
  }

  @Test
  void testGetServerPort() throws UnsupportedEncodingException {
    when(jakartaRequest.getServerPort()).thenReturn(1234);
    assertEquals(1234, cut.getServerPort());
  }

  @Test
  void testGetReader() throws IOException {
    final BufferedReader reader = mock(BufferedReader.class);
    when(jakartaRequest.getReader()).thenReturn(reader);
    assertEquals(reader, cut.getReader());
  }

  @Test
  void testGetRemoteAddr() throws IOException {
    when(jakartaRequest.getRemoteAddr()).thenReturn("Test");
    assertEquals("Test", cut.getRemoteAddr());
  }

  @Test
  void testGetRemoteHost() throws IOException {
    when(jakartaRequest.getRemoteHost()).thenReturn("remotehost");
    assertEquals("remotehost", cut.getRemoteHost());
  }

  @Test
  void testGetRemotePort() throws IOException {
    when(jakartaRequest.getRemotePort()).thenReturn(80);
    assertEquals(80, cut.getRemotePort());
  }

  @Test
  void testRemoveAttribute() throws IOException {
    cut.removeAttribute("Attribute Name");
    verify(jakartaRequest).removeAttribute("Attribute Name");
  }

  @Test
  void testSetAttribute() {
    cut.setAttribute("Attribute Name", 110);
    verify(jakartaRequest).setAttribute("Attribute Name", 110);
  }

  @Test
  void testGetLocale() throws IOException {
    when(jakartaRequest.getLocale()).thenReturn(Locale.FRENCH);
    assertEquals(Locale.FRENCH, cut.getLocale());
  }

  @Test
  void testGetLocales() throws IOException {
    @SuppressWarnings("unchecked")
    final Enumeration<Locale> enumeration = mock(Enumeration.class);
    when(jakartaRequest.getLocales()).thenReturn(enumeration);
    assertEquals(enumeration, cut.getLocales());
  }

  @Test
  void testIsSecure() throws IOException {
    when(jakartaRequest.isSecure()).thenReturn(true);
    assertEquals(true, cut.isSecure());
  }

  @Test
  void testGetLocalName() throws IOException {
    when(jakartaRequest.getLocalName()).thenReturn("Local Name");
    assertEquals("Local Name", cut.getLocalName());
  }

  @Test
  void testGetLocalAddr() throws IOException {
    when(jakartaRequest.getLocalAddr()).thenReturn("Local Addresse");
    assertEquals("Local Addresse", cut.getLocalAddr());
  }

  @Test
  void testGetLocalPort() throws IOException {
    when(jakartaRequest.getLocalPort()).thenReturn(9009);
    assertEquals(9009, cut.getLocalPort());
  }

  @Test
  void testGetRealPath() throws IOException {
    assertThrows(IllegalAccessError.class, () -> cut.getRealPath(PATH));
  }

  @Test
  void testGetRequestDispatcherThrows() throws IOException {
    assertThrows(IllegalAccessError.class, () -> cut.getRequestDispatcher(PATH));
  }

  @Test
  void testGetServletContextThrows() throws IOException {
    assertThrows(IllegalAccessError.class, () -> cut.getServletContext());
  }

  @Test
  void testStartAsyncThrows() throws IOException {
    assertThrows(IllegalAccessError.class, () -> cut.startAsync());
  }

  @Test
  void testStartAsyncParameterThrows() throws IOException {
    final ServletRequest servletRequest = mock(ServletRequest.class);
    final ServletResponse servletResponse = mock(ServletResponse.class);
    assertThrows(IllegalAccessError.class, () -> cut.startAsync(servletRequest, servletResponse));
  }

  @Test
  void testIsAsyncStarted() {
    when(jakartaRequest.isAsyncStarted()).thenReturn(true);
    assertTrue(cut.isAsyncStarted());
  }

  @Test
  void testIsAsyncSupported() {
    when(jakartaRequest.isAsyncSupported()).thenReturn(true);
    assertTrue(cut.isAsyncSupported());
  }

  @Test
  void testGetAsyncContextThrows() throws IOException {
    assertThrows(IllegalAccessError.class, () -> cut.getAsyncContext());
  }

  @Test
  void testGetDispatcherTypeThrows() throws IOException {
    assertThrows(IllegalAccessError.class, () -> cut.getDispatcherType());
  }

  @Test
  void testGetWrapper() {
    assertEquals(jakartaRequest, cut.getWrapped());
  }

  @SuppressWarnings("removal")
  private Cookie buildCookie() {
    final Cookie cookie = new Cookie(COOKIE_NAME, COOKIE_VALUE);
    cookie.setAttribute(COOKIE_ATTRIBUTE, COOKIE_ATTRIBUTE_VALUE);
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
