package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

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
