package com.sap.olingo.jpa.processor.core.api.mapper;

import java.io.IOException;

import javax.servlet.ServletOutputStream;

public class JakartaServletOutputStream extends ServletOutputStream {

  private final jakarta.servlet.ServletOutputStream jakartaOutputStream;

  public JakartaServletOutputStream(final jakarta.servlet.ServletOutputStream outputStream) {
    this.jakartaOutputStream = outputStream;
  }

  @Override
  public void write(final int b) throws IOException {
    jakartaOutputStream.write(b);
  }

  @Override
  public void print(final String s) throws IOException {
    jakartaOutputStream.print(s);
  }
}
