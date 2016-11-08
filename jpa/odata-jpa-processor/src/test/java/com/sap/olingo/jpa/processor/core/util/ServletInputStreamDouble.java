package com.sap.olingo.jpa.processor.core.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

public class ServletInputStreamDouble extends ServletInputStream {
  private final InputStream stream;

  public ServletInputStreamDouble(ServletInputStream stream) {
    super();
    this.stream = stream;
  }

  public ServletInputStreamDouble(StringBuffer stream) {
    super();
    if (stream != null)
      this.stream = new ByteArrayInputStream(stream.toString().getBytes());
    else
      this.stream = null;
  }

  @Override
  public int read() throws IOException {
    return stream.read();
  }

}
