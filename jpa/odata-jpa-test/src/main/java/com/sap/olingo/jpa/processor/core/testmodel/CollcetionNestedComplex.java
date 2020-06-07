package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@Embeddable
public class CollcetionNestedComplex {

  @EdmIgnore
  @Column(name = "\"ID\"")
  private String iD;

  @Column(name = "\"Number\"")
  private Long number;

  @Embedded
  private CollcetionInnerComplex inner;

  public Long getNumber() {
    return number;
  }

  public void setNumber(final Long number) {
    this.number = number;
  }

  public CollcetionInnerComplex getInner() {
    return inner;
  }

  public void setInner(final CollcetionInnerComplex inner) {
    this.inner = inner;
  }
}
