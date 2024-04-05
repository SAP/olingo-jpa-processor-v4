package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"Collections\"")
public class CollectionWithTwoKey {

  @Id
  @Column(name = "\"ID\"")
  private String iD;

  @Column(name = "\"Number\"")
  private Long number;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"NestedComplex\"",
      joinColumns = {
          @JoinColumn(name = "\"ID\"", referencedColumnName = "\"ID\""),
          @JoinColumn(name = "\"Number\"", referencedColumnName = "\"Number\"")
      })
  private List<CollectionNestedComplex> nested; // Must not be assigned to an ArrayList

  public String getID() {
    return iD;
  }

  public void setID(final String ID) {
    this.iD = ID;
  }

  @Override
  public int hashCode() {
    return Objects.hash(iD, number);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof final CollectionWithTwoKey other)
      return Objects.equals(iD, other.iD) && Objects.equals(number, other.number);
    return false;
  }

}
