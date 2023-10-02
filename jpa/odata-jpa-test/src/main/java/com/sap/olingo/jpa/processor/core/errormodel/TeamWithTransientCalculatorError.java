package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@Entity(name = "TeamWithTransientCalculatorError")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class TeamWithTransientCalculatorError {

  @Id
  @Column(name = "\"TeamKey\"")
  private String iD;

  @Column(name = "\"Name\"")
  private String name;

  @EdmTransient(requiredAttributes = { "name" }, calculator = TransientPropertyCalculatorTwoConstructors.class)
  private String completeName;
}
