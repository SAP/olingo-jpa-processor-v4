package com.sap.olingo.jpa.processor.core.testobjects;

public class ClassWithMultipleKeysSetter {
  private String id1;
  private Integer id2;
  private String id3;

  public ClassWithMultipleKeysSetter(final String id1, final String id3, final Integer id2) {
    super();
    this.id1 = id1;
    this.id2 = id2;
    this.id3 = id3;
  }

  public ClassWithMultipleKeysSetter(final String willi, final Integer id2, final String id3) {
    super();
    this.id1 = willi;
    this.id2 = id2;
    this.id3 = id3;
  }

  public ClassWithMultipleKeysSetter() {
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

  public void setId1(final String id1) {
    this.id1 = id1;
  }

  public void setId2(final Integer id2) {
    this.id2 = id2;
  }

  public void setId3(final String id3) {
    this.id3 = id3;
  }
}
