package com.sap.olingo.jpa.processor.core.testobjects;

public class ClassWithMultipleKeysConstructor {
  private final String id1;
  private final Integer id2;
  private final String id3;

  public ClassWithMultipleKeysConstructor(final String id1, final String id3, final Integer id2) {
    super();
    this.id1 = id1;
    this.id2 = id2;
    this.id3 = id3;
  }

  public ClassWithMultipleKeysConstructor(final String willi, final Integer id2, final String id3) {
    super();
    this.id1 = willi;
    this.id2 = id2;
    this.id3 = id3;
  }

  public ClassWithMultipleKeysConstructor() {
    this.id1 = "";
    this.id2 = 0;
    this.id3 = "";
  }

  public String getId1() {
    return id1;
  }

  public Integer getId2() {
    return id2;
  }

  public String getId3() {
    return id3;
  }
}
