package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "JoinTarget")
@Table(schema = "\"OLINGO\"", name = "\"JoinTarget\"")
public class JoinTarget {
  @Id
  @Column(name = "\"TargetKey\"")
  private Integer targetID;
}
