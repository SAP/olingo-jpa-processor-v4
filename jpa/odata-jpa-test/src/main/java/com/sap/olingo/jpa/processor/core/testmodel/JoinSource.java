package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
