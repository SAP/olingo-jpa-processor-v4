package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "SalesTeam")
@Table(schema = "\"OLINGO\"", name = "\"SalesTeam\"")
public class SalesTeam extends Group {

  @Column(name = "\"SalesArea\"")
  private String salesArea;

  @Override
  public boolean equals(final Object object) {
    return super.equals(object);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
