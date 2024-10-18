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
  public boolean equals(final Object object) {
    if (object instanceof final CollectionDeep other) {
      return iD != null ? iD.equals(other.iD) : other.iD == null;
    }
    return false;
  }

}
