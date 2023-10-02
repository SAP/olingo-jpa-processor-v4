package com.sap.olingo.jpa.processor.core.testobjects;

public class ClassWithIdClassConstructor {
  @SuppressWarnings("unused")
  private final ClassWithMultipleKeysSetter key;

  public ClassWithIdClassConstructor(final ClassWithMultipleKeysSetter key) {
    this.key = key;
  }

  public ClassWithIdClassConstructor() {
    key = null;
  }

  public ClassWithIdClassConstructor(final String id1, final String id3, final Integer id2) {
    super();
    this.key = new ClassWithMultipleKeysSetter(id1, id3, id2);
  }

  public ClassWithMultipleKeysSetter getKey() {
    return key;
  }
}
