package com.sap.olingo.jpa.processor.core.testobjects;

import java.util.UUID;

public class ClassWithOneKeyAndEmptyConstructor {
  private final UUID key;

  public ClassWithOneKeyAndEmptyConstructor(final String key) {
    this.key = UUID.fromString(key);
  }

  public ClassWithOneKeyAndEmptyConstructor() {
    this.key = UUID.randomUUID();
  }

  public ClassWithOneKeyAndEmptyConstructor(final UUID key) {
    this.key = key;
  }

  public UUID getKey() {
    return key;
  }
}
