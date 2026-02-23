package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;

@Embeddable
public class JoinComplex {

  @Column(name = "\"Number\"")
  private Long number;

  @OneToMany
  @JoinTable(name = "\"JoinRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\"", referencedColumnName = "\"SourceKey\""),
      inverseJoinColumns = @JoinColumn(name = "\"TargetID\""))
  private List<JoinTarget> oneToManyComplex;

  public JoinComplex() {

  }

  public JoinComplex(final Long number) {
    super();
    this.number = number;
  }

  public Long getNumber() {
    return number;
  }

  public void setNumber(final Long number) {
    this.number = number;
  }

  public List<JoinTarget> getOneToManyComplex() {
    return oneToManyComplex;
  }

  public void setOneToManyComplex(final List<JoinTarget> oneToManyComplex) {
    this.oneToManyComplex = oneToManyComplex;
  }
}
