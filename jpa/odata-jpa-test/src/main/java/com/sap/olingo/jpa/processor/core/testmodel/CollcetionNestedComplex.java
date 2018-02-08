package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class CollcetionNestedComplex {

  @Column(name = "\"Number\"")
  private Long number;

  @Embedded
  private CollcetionInnerComplex inner;
}
