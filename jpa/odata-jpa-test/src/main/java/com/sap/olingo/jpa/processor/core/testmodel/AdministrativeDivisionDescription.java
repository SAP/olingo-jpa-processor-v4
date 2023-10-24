package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public AdministrativeDivisionDescriptionKey getKey() {
    return key;
  }

  public void setKey(final AdministrativeDivisionDescriptionKey key) {
    this.key = key;
  }
}
