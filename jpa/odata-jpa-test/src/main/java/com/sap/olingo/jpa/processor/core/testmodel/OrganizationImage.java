package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;

@Entity(name = "OrganizationImage")
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::OrganizationImage\"")
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

  String getID() {
    return ID;
  }

  void setID(String iD) {
    ID = iD;
  }

  byte[] getImage() {
    return image;
  }

  void setImage(byte[] image) {
    this.image = image;
  }

  AdministrativeInformation getAdministrativeInformation() {
    return administrativeInformation;
  }

  void setAdministrativeInformation(AdministrativeInformation administrativeInformation) {
    this.administrativeInformation = administrativeInformation;
  }

  String getMimeType() {
    return mimeType;
  }

  void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
}
