package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigInteger;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import com.sap.olingo.jpa.metadata.converter.TimeInstantLongConverter;

@Embeddable
public class CollectionInnerComplex {

  @Column(name = "\"Figure1\"")
  private Long figure1;

  @Column(name = "\"Figure2\"")
  @Convert(converter = TimeInstantLongConverter.class)
  private Instant figure2;

  @Column(name = "\"Figure3\"")
  private BigInteger figure3;

  public Long getFigure1() {
    return figure1;
  }

  public void setFigure1(final Long figure1) {
    this.figure1 = figure1;
  }

  public Instant getFigure2() {
    return figure2;
  }

  public void setFigure2(final Instant figure2) {
    this.figure2 = figure2;
  }

  public BigInteger getFigure3() {
    return figure3;
  }

  public void setFigure3(final BigInteger figure3) {
    this.figure3 = figure3;
  }

}
