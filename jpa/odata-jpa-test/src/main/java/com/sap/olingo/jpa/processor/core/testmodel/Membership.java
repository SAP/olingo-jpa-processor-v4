package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity
@Table(schema = "\"OLINGO\"", name = "\"Membership\"")
@IdClass(MembershipKey.class)
public class Membership {
  @Id
  @Column(name = "\"PersonID\"", length = 32)
  private String personID;
  @Id
  @Column(name = "\"TeamID\"", length = 32)
  private String teamID;
}
