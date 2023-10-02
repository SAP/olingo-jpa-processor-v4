package com.sap.olingo.jpa.processor.core.api.mapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import jakarta.servlet.http.HttpServletRequest;

public class JakartaRequestMapper implements javax.servlet.http.HttpServletRequest {

  private final HttpServletRequest jakartaRequest;

  public JakartaRequestMapper(@Nonnull final HttpServletRequest jakartaRequest) {
    super();
    this.jakartaRequest = Objects.requireNonNull(jakartaRequest);
  }

  @Override
  public Object getAttribute(final String name) {
    return jakartaRequest.getAttribute(name);
  }

  @Override
  public Enumeration<String> getAttributeNames() {
    return jakartaRequest.getAttributeNames();
  }

  @Override
  public String getCharacterEncoding() {
    return jakartaRequest.getCharacterEncoding();
  }

  @Override
  public void setCharacterEncoding(final String env) throws UnsupportedEncodingException {
    jakartaRequest.setCharacterEncoding(env);
  }

  @Override
  public int getContentLength() {
    return jakartaRequest.getContentLength();
  }

  @Override
  public String getContentType() {
    return jakartaRequest.getContentType();
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    return new JakartaServletInputStream(jakartaRequest.getInputStream());
  }

  @Override
  public String getParameter(final String name) {
    return jakartaRequest.getParameter(name);
  }

  @Override
  public Enumeration<String> getParameterNames() {
    return jakartaRequest.getParameterNames();
  }

  @Override
  public String[] getParameterValues(final String name) {
    return jakartaRequest.getParameterValues(name);
  }

  @Override
  public Map<String, String[]> getParameterMap() {
    return jakartaRequest.getParameterMap();
  }

  @Override
  public String getProtocol() {
    return jakartaRequest.getProtocol();
  }

  @Override
  public String getScheme() {
    return jakartaRequest.getScheme();
  }

  @Override
  public String getServerName() {
    return jakartaRequest.getServerName();
  }

  @Override
  public int getServerPort() {
    return jakartaRequest.getServerPort();
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return jakartaRequest.getReader();
  }

  @Override
  public String getRemoteAddr() {
    return jakartaRequest.getRemoteAddr();
  }

  @Override
  public String getRemoteHost() {
    return jakartaRequest.getRemoteHost();
  }

  @Override
  public void setAttribute(final String name, final Object o) {
    jakartaRequest.setAttribute(name, o);
  }

  @Override
  public void removeAttribute(final String name) {
    jakartaRequest.removeAttribute(name);
  }

  @Override
  public Locale getLocale() {
    return jakartaRequest.getLocale();
  }

  @Override
  public Enumeration<Locale> getLocales() {
    return jakartaRequest.getLocales();
  }

  @Override
  public boolean isSecure() {
    return jakartaRequest.isSecure();
  }

  @Override
  public RequestDispatcher getRequestDispatcher(final String path) {
    throw new IllegalAccessError();
  }

  @Override
  public String getRealPath(final String path) {
    throw new IllegalAccessError();
  }

  @Override
  public int getRemotePort() {
    return jakartaRequest.getRemotePort();
  }

  @Override
  public String getLocalName() {
    return jakartaRequest.getLocalName();
  }

  @Override
  public String getLocalAddr() {

    return jakartaRequest.getLocalAddr();
  }

  @Override
  public int getLocalPort() {
    return jakartaRequest.getLocalPort();
  }

  @Override
  public ServletContext getServletContext() {
    throw new IllegalAccessError();
  }

  @Override
  public AsyncContext startAsync() throws IllegalStateException {
    throw new IllegalAccessError();
  }

  @Override
  public AsyncContext startAsync(final ServletRequest servletRequest, final ServletResponse servletResponse)
      throws IllegalStateException {
    throw new IllegalAccessError();
  }

  @Override
  public boolean isAsyncStarted() {
    return jakartaRequest.isAsyncStarted();
  }

  @Override
  public boolean isAsyncSupported() {
    return jakartaRequest.isAsyncSupported();
  }

  @Override
  public AsyncContext getAsyncContext() {
    throw new IllegalAccessError();
  }

  @Override
  public DispatcherType getDispatcherType() {
    throw new IllegalAccessError();
  }

  @Override
  public String getAuthType() {
    return jakartaRequest.getAuthType();
  }

  @Override
  public Cookie[] getCookies() {
    final Cookie[] cookies = new Cookie[jakartaRequest.getCookies().length];
    for (int i = 0; i < jakartaRequest.getCookies().length; i++) {
      final Cookie cookie = new Cookie(jakartaRequest.getCookies()[i].getName(), jakartaRequest.getCookies()[i]
          .getValue());
      cookie.setHttpOnly(jakartaRequest.getCookies()[i].isHttpOnly());
      cookie.setSecure(jakartaRequest.getCookies()[i].getSecure());
      cookie.setComment(jakartaRequest.getCookies()[i].getComment());
      cookie.setDomain(jakartaRequest.getCookies()[i].getDomain());
      cookie.setMaxAge(jakartaRequest.getCookies()[i].getMaxAge());
      cookie.setPath(jakartaRequest.getCookies()[i].getPath());
      cookie.setVersion(jakartaRequest.getCookies()[i].getVersion());
      cookies[i] = cookie;
    }
    return cookies;
  }

  @Override
  public long getDateHeader(final String name) {
    return jakartaRequest.getDateHeader(name);
  }

  @Override
  public String getHeader(final String name) {
    return jakartaRequest.getHeader(name);
  }

  @Override
  public Enumeration<String> getHeaders(final String name) {
    return jakartaRequest.getHeaders(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return jakartaRequest.getHeaderNames();
  }

  @Override
  public int getIntHeader(final String name) {
    return jakartaRequest.getIntHeader(name);
  }

  @Override
  public String getMethod() {
    return jakartaRequest.getMethod();
  }

  @Override
  public String getPathInfo() {
    return jakartaRequest.getPathInfo();
  }

  @Override
  public String getPathTranslated() {
    return jakartaRequest.getPathTranslated();
  }

  @Override
  public String getContextPath() {
    return jakartaRequest.getContextPath();
  }

  @Override
  public String getQueryString() {
    return jakartaRequest.getQueryString();
  }

  @Override
  public String getRemoteUser() {
    return jakartaRequest.getRemoteUser();
  }

  @Override
  public boolean isUserInRole(final String role) {
    return jakartaRequest.isUserInRole(role);
  }

  @Override
  public Principal getUserPrincipal() {
    return jakartaRequest.getUserPrincipal();
  }

  @Override
  public String getRequestedSessionId() {
    return jakartaRequest.getRequestedSessionId();
  }

  @Override
  public String getRequestURI() {
    return jakartaRequest.getRequestURI();
  }

  @Override
  public StringBuffer getRequestURL() {
    return jakartaRequest.getRequestURL();
  }

  @Override
  public String getServletPath() {
    return jakartaRequest.getServletPath();
  }

  @Override
  public HttpSession getSession(final boolean create) {
    throw new IllegalAccessError();
  }

  @Override
  public HttpSession getSession() {
    throw new IllegalAccessError();
  }

  @Override
  public boolean isRequestedSessionIdValid() {
    return jakartaRequest.isRequestedSessionIdValid();
  }

  @Override
  public boolean isRequestedSessionIdFromCookie() {
    return jakartaRequest.isRequestedSessionIdFromCookie();
  }

  @Override
  public boolean isRequestedSessionIdFromURL() {
    return jakartaRequest.isRequestedSessionIdFromURL();
  }

  @Override
  public boolean isRequestedSessionIdFromUrl() {
    return jakartaRequest.isRequestedSessionIdFromURL();
  }

  @Override
  public boolean authenticate(final HttpServletResponse response) throws IOException, ServletException {
    return false;
  }

  @Override
  public void login(final String userName, final String password) throws ServletException {
    try {
      jakartaRequest.login(userName, password);
    } catch (final jakarta.servlet.ServletException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void logout() throws ServletException {
    try {
      jakartaRequest.logout();
    } catch (final jakarta.servlet.ServletException e) {
      throw new ServletException(e);
    }
  }

  @Override
  public Collection<Part> getParts() throws IOException, ServletException {
    throw new IllegalAccessError();
  }

  @Override
  public Part getPart(final String name) throws IOException, ServletException {
    throw new IllegalAccessError();
  }

  public HttpServletRequest getWrapped() {
    return jakartaRequest;
  }
}
