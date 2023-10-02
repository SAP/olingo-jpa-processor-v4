package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@Embeddable
public class CollectionNestedComplex {

  @EdmIgnore
  @Column(name = "\"ID\"")
  private String iD;

  @Column(name = "\"Number\"")
  private Long number;

  @Embedded
  private CollectionInnerComplex inner;

  public Long getNumber() {
    return number;
  }

  public void setNumber(final Long number) {
    this.number = number;
  }

  public CollectionInnerComplex getInner() {
    return inner;
  }

  public void setInner(final CollectionInnerComplex inner) {
    this.inner = inner;
  }
}
