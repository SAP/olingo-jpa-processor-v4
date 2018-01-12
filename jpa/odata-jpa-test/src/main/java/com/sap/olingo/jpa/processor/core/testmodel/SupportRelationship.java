package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"SupportRelationship\"")
public class SupportRelationship {
  @Id
  @Column(name = "\"ID\"")
  private Integer iD;

  @Column(name = "\"OrganizationID\"", length = 32)
  private String organizationID;

  @Column(name = "\"PersonID\"", length = 32)
  private String personID;
}
