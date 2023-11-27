package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity(name = "Team")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class Team {

  @Id
  @Column(name = "\"TeamKey\"")
  private String iD;

  @Column(name = "\"Name\"")
  private String name;

  @ManyToMany(mappedBy = "teams")
  private List<Person> member;

  public Team(final String iD) {
    super();
    this.iD = iD;
  }

  public Team() {}

  @Override
  public String toString() {
    return "Team [iD=" + iD + ", name=" + name + ", member=" + member + "]";
  }
}
