package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "JoinTarget")
@Table(schema = "\"OLINGO\"", name = "\"JoinTarget\"")
public class JoinTarget {
  @Id
  @Column(name = "\"TargetKey\"")
  private Integer targetID;

  @ManyToOne()
  @JoinTable(name = "\"JoinRelation\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"TargetID\""),
      inverseJoinColumns = @JoinColumn(name = "\"SourceID\""))
  private JoinSource manyToOne;
}
