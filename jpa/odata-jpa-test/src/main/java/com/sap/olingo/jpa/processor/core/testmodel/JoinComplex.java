package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;

@Embeddable
public class JoinComplex {

  @Column(name = "\"Number\"")
  private Long number;

  @OneToMany
  @JoinTable(name = "\"JoinRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\"", referencedColumnName = "\"SourceKey\""),
      inverseJoinColumns = @JoinColumn(name = "\"TargetID\""))
  private List<JoinTarget> oneToManyComplex;
}
