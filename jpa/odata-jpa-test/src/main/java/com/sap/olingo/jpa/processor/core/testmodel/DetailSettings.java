package com.sap.olingo.jpa.processor.core.testmodel;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class DetailSettings {

  @Column(name = "\"Id\"")
  private Integer id;

  @Column(name = "\"Name\"", length = 255)
  private String name;

  @EdmIgnore
  @Column(name = "\"GeneralName\"", length = 255, insertable = true, updatable = false)
  private String generalName;

}
