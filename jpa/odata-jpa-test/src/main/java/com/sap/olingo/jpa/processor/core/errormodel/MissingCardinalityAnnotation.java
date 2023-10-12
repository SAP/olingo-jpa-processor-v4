package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;

@Entity()
public class MissingCardinalityAnnotation {

  @Id
  @Column(name = "\"ID\"")
  protected String id;

  @JoinTable(name = "\"Membership\"", schema = "\"OLINGO\"",
      joinColumns = @JoinColumn(name = "\"PersonID\""),
      inverseJoinColumns = @JoinColumn(name = "\"TeamID\""))
  private List<Team> teams;

  @JoinColumn(name = "\"TeamKey\"")
  private Team oneTeam;

}
