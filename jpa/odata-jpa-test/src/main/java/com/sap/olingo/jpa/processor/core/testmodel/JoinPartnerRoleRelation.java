package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity(name = "JoinPartnerRoleRelation")
@Table(schema = "\"OLINGO\"", name = "\"JoinPartnerRoleRelation\"")

public class JoinPartnerRoleRelation {
  @EmbeddedId
  private JoinPartnerRoleRelationKey key;
}
