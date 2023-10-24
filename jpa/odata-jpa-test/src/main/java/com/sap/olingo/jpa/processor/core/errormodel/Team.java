package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

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

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public List<NavigationAttributeProtected> getMember() {
    return member;
  }

  public void setMember(final List<NavigationAttributeProtected> member) {
    this.member = member;
  }

  @Override
  public int hashCode() {
    return Objects.hash(iD, member, name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final Team other = (Team) obj;
    return Objects.equals(iD, other.iD) && Objects.equals(member, other.member) && Objects.equals(name, other.name);
  }
}
