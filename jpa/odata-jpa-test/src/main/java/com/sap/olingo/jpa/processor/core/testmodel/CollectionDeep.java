package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"CollectionsDeep\"")
public class CollectionDeep {

  @Id
  @Column(name = "\"ID\"")
  private String iD;

  // Collection as part of nested complex
  @Embedded
  private CollectionFirstLevelComplex firstLevel;

  public void setID(final String iD) {
    this.iD = iD;
  }

  public void setFirstLevel(final CollectionFirstLevelComplex firstLevel) {
    this.firstLevel = firstLevel;
  }

  public String getID() {
    return iD;
  }

  public CollectionFirstLevelComplex getFirstLevel() {
    return firstLevel;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iD == null) ? 0 : iD.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final CollectionDeep other = (CollectionDeep) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
