package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"AdministrativeDivisionDescription\"")
public class AdministrativeDivisionDescription implements KeyAccess {

  @EmbeddedId
  private AdministrativeDivisionDescriptionKey key;

  @EdmSearchable
  @Column(name = "\"Name\"", length = 100, updatable = false)
  private String name;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public AdministrativeDivisionDescriptionKey getKey() {
    return key;
  }

  public void setKey(AdministrativeDivisionDescriptionKey key) {
    this.key = key;
  }
}
