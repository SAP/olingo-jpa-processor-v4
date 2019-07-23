package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity(name = "ErrorTeam")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class Team {

  @Id
  @Column(name = "\"TeamKey\"")
  private String iD;

  @Column(name = "\"Name\"")
  private String name;

  @ManyToMany(mappedBy = "teams")
  private List<NavigationAttributeProtected> member;
}
