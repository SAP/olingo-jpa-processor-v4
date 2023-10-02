package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;

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

