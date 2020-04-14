package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;
import com.sap.olingo.jpa.processor.core.errormodel.DummyPropertyCalculator;

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
  private List<CollcetionNestedComplexWithTransient> nested; // Must not be assigned to an ArrayList
  @Transient
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private List<String> transientComment;

  public String getID() {
    return iD;
  }

  public void setID(String iD) {
    this.iD = iD;
  }

  public CollectionPartOfComplex getComplex() {
    return complex;
  }

  public void setComplex(CollectionPartOfComplex complex) {
    this.complex = complex;
  }

  public List<CollcetionNestedComplexWithTransient> getNested() {
    return nested;
  }

  public void setNested(List<CollcetionNestedComplexWithTransient> nested) {
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
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    CollectionWithTransient other = (CollectionWithTransient) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
