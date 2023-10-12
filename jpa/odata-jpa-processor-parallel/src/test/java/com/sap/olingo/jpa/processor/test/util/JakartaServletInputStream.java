package com.sap.olingo.jpa.processor.test.util;

import java.io.IOException;

import javax.servlet.ServletInputStream;

public class JakartaServletInputStream extends ServletInputStream {
  private final jakarta.servlet.ServletInputStream inputStream;

  public JakartaServletInputStream(final jakarta.servlet.ServletInputStream inputStream) {
    this.inputStream = inputStream;
  }

  @Override
  public int read() throws IOException {
    return inputStream.read();
  }

  @Override
  public int readLine(final byte[] b, final int off, final int len) throws IOException {
    return inputStream.readLine(b, off, len);
  }

}
