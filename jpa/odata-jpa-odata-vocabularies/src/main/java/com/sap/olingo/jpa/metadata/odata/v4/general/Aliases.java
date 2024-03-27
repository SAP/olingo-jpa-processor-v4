package com.sap.olingo.jpa.metadata.odata.v4.general;

import com.sap.olingo.jpa.metadata.core.edm.extension.vocabularies.AliasAccess;

public enum Aliases implements AliasAccess {

  CORE("Core"),
  CAPABILITIES("Capabilities");

  private final String alias;

  Aliases(final String alias) {
    this.alias = alias;
  }

  @Override
  public String alias() {
    return alias;
  }

}
