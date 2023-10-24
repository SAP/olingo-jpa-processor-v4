package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;

@Entity(name = "PersonImage")
@Table(schema = "\"OLINGO\"", name = "\"PersonImage\"")
public class PersonImage {
  @Id
  @Column(name = "\"ID\"")
  private String ID;

  @Column(name = "\"Image\"")
  @EdmMediaStream(contentType = "image/png")
  private byte[] image;

  @Embedded
  private AdministrativeInformation administrativeInformation = new AdministrativeInformation();

  String getID() {
    return ID;
  }

  void setID(final String iD) {
    ID = iD;
  }

  byte[] getImage() {
    return image.clone();
  }

  void setImage(final byte[] image) {
    this.image = image;
  }

  AdministrativeInformation getAdministrativeInformation() {
    return administrativeInformation;
  }

  void setAdministrativeInformation(final AdministrativeInformation administrativeInformation) {
    this.administrativeInformation = administrativeInformation;
  }
}
