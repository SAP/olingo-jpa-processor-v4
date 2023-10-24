package com.sap.olingo.jpa.processor.core.api.mapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class JakartaResponseMapper implements HttpServletResponse {

  private final jakarta.servlet.http.HttpServletResponse jakartaResponse;

  public JakartaResponseMapper(final jakarta.servlet.http.HttpServletResponse response) {
    this.jakartaResponse = response;
  }

  @Override
  public String getCharacterEncoding() {
    return jakartaResponse.getCharacterEncoding();
  }

  @Override
  public String getContentType() {
    return jakartaResponse.getContentType();
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return new JakartaServletOutputStream(jakartaResponse.getOutputStream());
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return jakartaResponse.getWriter();
  }

  @Override
  public void setCharacterEncoding(final String charset) {
    jakartaResponse.setCharacterEncoding(charset);
  }

  @Override
  public void setContentLength(final int len) {
    jakartaResponse.setContentLength(len);
  }

  @Override
  public void setContentType(final String type) {
    jakartaResponse.setContentType(type);
  }

  @Override
  public void setBufferSize(final int size) {
    jakartaResponse.setBufferSize(size);
  }

  @Override
  public int getBufferSize() {
    return jakartaResponse.getBufferSize();
  }

  @Override
  public void flushBuffer() throws IOException {
    jakartaResponse.flushBuffer();
  }

  @Override
  public void resetBuffer() {
    jakartaResponse.resetBuffer();
  }

  @Override
  public boolean isCommitted() {
    return jakartaResponse.isCommitted();
  }

  @Override
  public void reset() {
    jakartaResponse.reset();
  }

  @Override
  public void setLocale(final Locale loc) {
    jakartaResponse.setLocale(loc);
  }

  @Override
  public Locale getLocale() {
    return jakartaResponse.getLocale();
  }

  @SuppressWarnings("removal")
  @Override
  public void addCookie(final Cookie cookie) {
    final jakarta.servlet.http.Cookie newCookie =
        new jakarta.servlet.http.Cookie(cookie.getName(), cookie.getValue());

    newCookie.setHttpOnly(cookie.isHttpOnly());
    newCookie.setSecure(cookie.getSecure());
    newCookie.setComment(cookie.getComment());
    newCookie.setDomain(cookie.getDomain());
    newCookie.setMaxAge(cookie.getMaxAge());
    newCookie.setPath(cookie.getPath());
    newCookie.setVersion(cookie.getVersion());
    jakartaResponse.addCookie(newCookie);
  }

  @Override
  public boolean containsHeader(final String name) {
    return jakartaResponse.containsHeader(name);
  }

  @Override
  public String encodeURL(final String url) {
    return jakartaResponse.encodeURL(url);
  }

  @Override
  public String encodeRedirectURL(final String url) {
    return jakartaResponse.encodeRedirectURL(url);
  }

  @Override
  public String encodeUrl(final String url) {
    return jakartaResponse.encodeURL(url);
  }

  @Override
  public String encodeRedirectUrl(final String url) {
    return jakartaResponse.encodeRedirectURL(url);
  }

  @Override
  public void sendError(final int sc, final String msg) throws IOException {
    jakartaResponse.sendError(sc, msg);
  }

  @Override
  public void sendError(final int sc) throws IOException {
    jakartaResponse.sendError(sc);
  }

  @Override
  public void sendRedirect(final String location) throws IOException {
    jakartaResponse.sendRedirect(location);
  }

  @Override
  public void setDateHeader(final String name, final long date) {
    jakartaResponse.setDateHeader(name, date);
  }

  @Override
  public void addDateHeader(final String name, final long date) {
    jakartaResponse.addDateHeader(name, date);
  }

  @Override
  public void setHeader(final String name, final String value) {
    jakartaResponse.setHeader(name, value);
  }

  @Override
  public void addHeader(final String name, final String value) {
    jakartaResponse.addHeader(name, value);
  }

  @Override
  public void setIntHeader(final String name, final int value) {
    jakartaResponse.setIntHeader(name, value);
  }

  @Override
  public void addIntHeader(final String name, final int value) {
    jakartaResponse.addIntHeader(name, value);
  }

  @Override
  public void setStatus(final int status) {
    jakartaResponse.setStatus(status);
  }

  @Override
  public void setStatus(final int status, final String message) {
    jakartaResponse.setStatus(status);
  }

  @Override
  public int getStatus() {
    return jakartaResponse.getStatus();
  }

  @Override
  public String getHeader(final String name) {
    return jakartaResponse.getHeader(name);
  }

  @Override
  public Collection<String> getHeaders(final String name) {
    return jakartaResponse.getHeaders(name);
  }

  @Override
  public Collection<String> getHeaderNames() {
    return jakartaResponse.getHeaderNames();
  }

}
