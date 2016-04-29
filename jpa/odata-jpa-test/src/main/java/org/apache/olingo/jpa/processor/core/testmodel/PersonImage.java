package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmMediaStream;

@Entity(name = "PersonImage")
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::PersonImage\"")
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
}
