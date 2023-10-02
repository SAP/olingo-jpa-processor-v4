package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Entity(name = "JoinPartnerRoleRelation")
@Table(schema = "\"OLINGO\"", name = "\"JoinPartnerRoleRelation\"")

public class JoinPartnerRoleRelation {
  @EmbeddedId
  private JoinPartnerRoleRelationKey key;
}
