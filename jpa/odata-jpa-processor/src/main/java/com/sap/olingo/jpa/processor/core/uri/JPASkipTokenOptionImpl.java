package com.sap.olingo.jpa.processor.core.uri;

import org.apache.olingo.server.api.uri.queryoption.SkipTokenOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOptionKind;

record JPASkipTokenOptionImpl(String skipToken) implements SkipTokenOption {

  @Override
  public SystemQueryOptionKind getKind() {
    return SystemQueryOptionKind.SKIPTOKEN;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getText() {
    return skipToken;
  }

  @Override
  public String getValue() {
    return skipToken;
  }

}
