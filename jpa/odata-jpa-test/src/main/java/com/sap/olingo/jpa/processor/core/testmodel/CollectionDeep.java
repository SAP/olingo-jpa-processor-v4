package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"CollectionsDeep\"")
public class CollectionDeep {

  @Id
  @Column(name = "\"ID\"")
  private String iD;

  // Collection as part of nested complex
  @Embedded
  private CollectionFirstLevelComplex firstLevel;

  public void setID(String iD) {
    this.iD = iD;
  }

  public void setFirstLevel(CollectionFirstLevelComplex firstLevel) {
    this.firstLevel = firstLevel;
  }

  public String getID() {
    return iD;
  }

  public CollectionFirstLevelComplex getFirstLevel() {
    return firstLevel;
  }

}
