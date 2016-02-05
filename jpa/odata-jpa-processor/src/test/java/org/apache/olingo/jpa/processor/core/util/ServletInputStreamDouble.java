package org.apache.olingo.jpa.processor.core.util;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.ServletInputStream;

public class ServletInputStreamDouble extends ServletInputStream {
  private final ServletInputStream stream;

  public ServletInputStreamDouble(ServletInputStream stream) {
    super();
    this.stream = stream;
  }

  @Override
  public int read() throws IOException {
    // TODO
    fail();
    return stream.read();
  }

}
