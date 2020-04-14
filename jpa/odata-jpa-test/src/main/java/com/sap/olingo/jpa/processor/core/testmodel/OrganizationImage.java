package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;

@Entity(name = "OrganizationImage")
@Table(schema = "\"OLINGO\"", name = "\"OrganizationImage\"")
public class OrganizationImage {
  @Id
  @Column(name = "\"ID\"")
  private String ID;

  @Column(name = "\"Image\"")
  @EdmMediaStream(contentTypeAttribute = "mimeType")
  private byte[] image;

  @EdmIgnore
  @Column(name = "\"MimeType\"")
  private String mimeType;

  @Embedded
  private AdministrativeInformation administrativeInformation = new AdministrativeInformation();

  public String getID() {
    return ID;
  }

  public void setID(final String iD) {
    ID = iD;
  }

  public byte[] getImage() {
    return image;
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
    result = prime * result + ((ID == null) ? 0 : ID.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    OrganizationImage other = (OrganizationImage) obj;
    if (ID == null) {
      if (other.ID != null) return false;
    } else if (!ID.equals(other.ID)) return false;
    return true;
  }
}
