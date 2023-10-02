package com.sap.olingo.jpa.processor.core.testobjects;

public class ClassWithIdClassWithoutConstructor {
  @SuppressWarnings("unused")
  private final ClassWithMultipleKeysConstructor key;

  public ClassWithIdClassWithoutConstructor(final ClassWithMultipleKeysConstructor key) {
    this.key = key;
  }
}
