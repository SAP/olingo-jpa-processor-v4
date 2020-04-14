package com.sap.olingo.jpa.processor.cb.impl;

class AliasBuilder {
  private static final String ALIAS_PREFIX = "E";
  private int aliasCount = 0;
  private final String prefix;

  AliasBuilder() {
    this(ALIAS_PREFIX);
  }

  AliasBuilder(final String prefix) {
    this.prefix = prefix;
  }

  String getNext() {
    return prefix + aliasCount++;
  }
}
