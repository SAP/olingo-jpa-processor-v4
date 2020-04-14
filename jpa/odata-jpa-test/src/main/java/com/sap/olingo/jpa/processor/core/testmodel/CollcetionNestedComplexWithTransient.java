package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@Embeddable
public class CollcetionNestedComplexWithTransient {

  @Column(name = "\"Number\"")
  private Long number;

  @Transient
  @EdmTransient(requiredAttributes = "number", calculator = LogarithmCalculator.class)
  private Double log;

  @Embedded
  private CollcetionInnerComplex inner;

  public Long getNumber() {
    return number;
  }

  public void setNumber(Long number) {
    this.number = number;
  }

  public CollcetionInnerComplex getInner() {
    return inner;
  }

  public void setInner(CollcetionInnerComplex inner) {
    this.inner = inner;
  }
}
