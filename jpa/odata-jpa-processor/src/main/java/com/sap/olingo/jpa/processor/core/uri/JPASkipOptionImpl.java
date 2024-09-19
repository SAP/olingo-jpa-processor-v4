package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.server.api.uri.queryoption.SkipOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

public record JPASkipOptionImpl(int value) implements SkipOption {

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getText() {
    return String.valueOf(value);
  }

  @Override
  public int getValue() {
    return value;
  }

  @Override
  public SystemQueryOptionKind getKind() {
    return SystemQueryOptionKind.SKIP;
  }

}
