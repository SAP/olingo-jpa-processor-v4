package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@Embeddable
public class CollectionNestedComplexWithTransient {

  @EdmIgnore
  @Column(name = "\"Number\"")
  private Long number;

  @Transient
  @EdmTransient(requiredAttributes = "number", calculator = LogarithmCalculator.class)
  private Double log;

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
