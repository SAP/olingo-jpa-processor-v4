package com.sap.olingo.jpa.processor.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

public class ServletInputStreamDouble extends ServletInputStream {
  private final InputStream stream;

  public ServletInputStreamDouble(final ServletInputStream stream) {
    super();
    this.stream = stream;
  }

  public ServletInputStreamDouble(final StringBuilder stream) {
    super();
    if (stream != null)
      this.stream = new ByteArrayInputStream(stream.toString().getBytes(StandardCharsets.UTF_8));
    else
      this.stream = null;
  }

  @Override
  public int read() throws IOException {
    return stream.read();
  }

  @Override
  public boolean isFinished() {
    return false;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(final ReadListener readListener) {
    throw new IllegalAccessError();
  }

}
