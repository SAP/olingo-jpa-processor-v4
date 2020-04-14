package com.sap.olingo.jpa.processor.core.errormodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmProtectedBy;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class ComplextProtectedWrongPath {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Embedded
  @EdmProtectedBy(name = "UserId", path = "created/wrong")
  private AdministrativeInformation administrativeInformation = new AdministrativeInformation();

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
    ComplextProtectedWrongPath other = (ComplextProtectedWrongPath) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

  public String getID() {
    return iD;
  }

  public void setID(String iD) {
    this.iD = iD;
  }

  public long getETag() {
    return eTag;
  }

  public void setETag(long eTag) {
    this.eTag = eTag;
  }
}
