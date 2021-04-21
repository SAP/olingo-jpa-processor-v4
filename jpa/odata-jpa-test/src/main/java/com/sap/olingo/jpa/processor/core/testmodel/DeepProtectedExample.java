package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity(name = "ProtectionExample")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
public class DeepProtectedExample {

  @Id
  @Column(name = "\"ID\"")
  private String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  private long eTag;

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  private String type;

  @Embedded
  private AddressDeepThreeProtections postalAddress;

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public AddressDeepThreeProtections getPostalAddress() {
    return postalAddress;
  }

  public void setPostalAddress(final AddressDeepThreeProtections postalAddress) {
    this.postalAddress = postalAddress;
  }

  public long getETag() {
    return eTag;
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
    final DeepProtectedExample other = (DeepProtectedExample) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }

}
