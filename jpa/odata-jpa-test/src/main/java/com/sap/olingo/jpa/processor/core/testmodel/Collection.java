package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.converter.OffsetDateTimeConverter;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"Collections\"")
public class Collection {

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
  private List<CollectionNestedComplex> nested; // Must not be assigned to an ArrayList

  @Convert(converter = OffsetDateTimeConverter.class)
  @Column(name = "\"Timestamp\"")
  private OffsetDateTime dateTime;

  @Convert(converter = LocalDateTimeConverter.class)
  @Column(name = "\"Timestamp\"", insertable = false, updatable = false)
  private LocalDateTime localDateTime;

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

  public List<CollectionNestedComplex> getNested() {
    return nested;
  }

  public void setNested(final List<CollectionNestedComplex> nested) {
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
    final Collection other = (Collection) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
