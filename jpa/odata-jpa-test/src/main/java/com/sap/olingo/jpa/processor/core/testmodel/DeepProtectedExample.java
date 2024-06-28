package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

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
  @Convert(converter = TimestampLongConverter.class)
  private Timestamp etag;

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

  public Timestamp getEtag() {
    return etag;
  }

  public void setEtag(final Timestamp timestamp) {
    etag = timestamp;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iD == null) ? 0 : iD.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof final DeepProtectedExample other) {
      if (iD == null) {
        if (other.iD == null)
          return true;
      } else {
        return iD.equals(other.iD);
      }
    }
    return false;
  }
}
