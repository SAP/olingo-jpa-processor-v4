package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CollcetionInnerComplex {

  @Column(name = "\"Figure1\"")
  private Long figure1;

  @Column(name = "\"Figure2\"")
  private Long figure2;

  @Column(name = "\"Figure3\"")
  private Long figure3;

}
