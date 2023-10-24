package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@Entity(name = "TeamWithTransientError")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class TeamWithTransientError {

  @Id
  @Column(name = "\"TeamKey\"")
  private String iD;

  @Column(name = "\"Name\"")
  private String name;

  @EdmTransient(requiredAttributes = { "name", "unknown" }, calculator = DummyPropertyCalculator.class)
  private String completeName;
}
