package com.sap.olingo.jpa.processor.core.testobjects;

import java.util.UUID;

public class ClassWithOneKeyConstructor {
  private final UUID key;

  public ClassWithOneKeyConstructor(final String key) {
    this.key = UUID.fromString(key);
  }

  public ClassWithOneKeyConstructor(final UUID key) {
    this.key = key;
  }

  public ClassWithOneKeyConstructor(final byte[] key) {
    this.key = UUID.nameUUIDFromBytes(key);
  }

  public UUID getKey() {
    return key;
  }
}
