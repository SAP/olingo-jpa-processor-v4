package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
