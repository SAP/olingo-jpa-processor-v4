package com.sap.olingo.jpa.processor.core.errormodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@IdClass(CompoundKey.class)
@Entity(name = "TeamWithTransientError")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class TeamWithTransientKey {

  @Id
  @Column(name = "\"TeamKey\"")
  private String iD;
  @Id
  @Transient
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private String name;
}
