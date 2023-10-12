package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;

@Entity(name = "OrganizationImage")
@Table(schema = "\"OLINGO\"", name = "\"OrganizationImage\"")
public class OrganizationImage {
  @Id
  @Column(name = "\"ID\"")
  private String iD;

  @Column(name = "\"Image\"")
  @EdmMediaStream(contentTypeAttribute = "mimeType")
  private byte[] image;

  @EdmIgnore
  @Column(name = "\"MimeType\"")
  private String mimeType;

  @Embedded
  private AdministrativeInformation administrativeInformation = new AdministrativeInformation();

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public byte[] getImage() {
    return image.clone(); // For sonar
  }

  public void setImage(final byte[] image) {
    this.image = image;
  }

  public AdministrativeInformation getAdministrativeInformation() {
    return administrativeInformation;
  }

  public void setAdministrativeInformation(final AdministrativeInformation administrativeInformation) {
    this.administrativeInformation = administrativeInformation;
  }

  String getMimeType() {
    return mimeType;
  }

  void setMimeType(final String mimeType) {
    this.mimeType = mimeType;
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
    final OrganizationImage other = (OrganizationImage) obj;
    if (iD == null) {
      if (other.iD != null) return false;
    } else if (!iD.equals(other.iD)) return false;
    return true;
  }
}
