package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class CollectionFirstLevelComplex {

  @Column(name = "\"LevelID\"")
  private Integer levelID;

  @Embedded
  private CollectionSecondLevelComplex secondLevel;

  public void setLevelID(Integer levelID) {
    this.levelID = levelID;
  }

  public void setSecondLevel(CollectionSecondLevelComplex secondLevel) {
    this.secondLevel = secondLevel;
  }

  public Integer getLevelID() {
    return levelID;
  }

  public CollectionSecondLevelComplex getSecondLevel() {
    return secondLevel;
  }
}
