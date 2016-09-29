package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmSearchable;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::AdministrativeDivisionDescription\"")
public class AdministrativeDivisionDescription {

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
}
