package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"NestedComplex\"")
@IdClass(NestedComplexKey.class)
public class NestedComplex {

  @EdmIgnore
  @Id
  @Column(name = "\"ID\"")
  private String iD;

  @Id
  @Column(name = "\"Number\"")
  private Long number;

  @Embedded
  private CollectionInnerComplex inner;

  public Long getNumber() {
    return number;
  }

  public void setNumber(final Long number) {
    this.number = number;
  }

  public CollectionInnerComplex getInner() {
    return inner;
  }

  public void setInner(final CollectionInnerComplex inner) {
    this.inner = inner;
  }

}
