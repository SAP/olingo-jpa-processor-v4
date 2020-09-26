package com.sap.olingo.jpa.processor.core.util.matcher;

import java.io.IOException;
import java.io.InputStream;

import org.mockito.ArgumentMatcher;

public class InputStreamMatcher implements ArgumentMatcher<InputStream> {

  private String extElement;
  private String input = null;

  public InputStreamMatcher(String pattern) {
    extElement = pattern;
  }

  @Override
  public boolean matches(final InputStream stream) {
    if (stream != null) {
      if (input == null) {
        byte[] buffer = new byte[1024];
        try {
          stream.read(buffer);
        } catch (IOException e) {
          return false;
        }
        input = new String(buffer);
      }
      return input.contains(extElement);
    }
    return false;
  }

}
