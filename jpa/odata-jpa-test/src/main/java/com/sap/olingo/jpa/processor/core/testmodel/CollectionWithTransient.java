package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"Collections\"")
public class CollectionWithTransient {

  @Id
  @Column(name = "\"ID\"")
  private String iD;

  // Collection as part of complex
  @Embedded
  private CollectionPartOfComplex complex;

  // Collection with nested complex
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"NestedComplex\"",
      joinColumns = @JoinColumn(name = "\"ID\""))
  private List<CollectionNestedComplexWithTransient> nested; // Must not be assigned to an ArrayList
  @Transient
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private List<String> transientComment;

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public CollectionPartOfComplex getComplex() {
    return complex;
  }

  public void setComplex(final CollectionPartOfComplex complex) {
    this.complex = complex;
  }

  public List<CollectionNestedComplexWithTransient> getNested() {
    return nested;
  }

  public void setNested(final List<CollectionNestedComplexWithTransient> nested) {
    this.nested = nested;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iD == null) ? 0 : iD.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final CollectionWithTransient other = (CollectionWithTransient) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
