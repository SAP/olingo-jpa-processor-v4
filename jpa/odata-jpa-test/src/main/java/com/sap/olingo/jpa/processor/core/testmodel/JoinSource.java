package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity(name = "JoinSource")
@Table(schema = "\"OLINGO\"", name = "\"JoinSource\"")
public class JoinSource {

  @Id
  @Column(name = "\"SourceKey\"")
  private Integer sourceID;

  @OneToMany
  @JoinTable(name = "\"JoinRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TargetID\""))
  private List<JoinTarget> oneToMany;

  @OneToMany
  @JoinTable(name = "\"JoinHiddenRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"SourceID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TargetID\""))
  private List<JoinTarget> oneToManyHidden;

  @Embedded
  private JoinComplex complex;
}
