package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity(name = "JoinRelation")
@Table(schema = "\"OLINGO\"", name = "\"JoinRelation\"")
public class JoinRelation {

  @EmbeddedId
  private JoinRelationKey key;
}
