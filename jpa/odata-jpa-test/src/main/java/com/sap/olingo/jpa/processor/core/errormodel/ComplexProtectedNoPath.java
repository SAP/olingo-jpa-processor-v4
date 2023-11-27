package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class ComplexProtectedNoPath {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Embedded
  @EdmProtectedBy(name = "UserId")
  private final AdministrativeInformation administrativeInformation = new AdministrativeInformation();

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
    final ComplexProtectedNoPath other = (ComplexProtectedNoPath) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

  public String getID() {
    return iD;
  }

  public long getETag() {
    return eTag;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public void setETag(final long eTag) {
    this.eTag = eTag;
  }

}
