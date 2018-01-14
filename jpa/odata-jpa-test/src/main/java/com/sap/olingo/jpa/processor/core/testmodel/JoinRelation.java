package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "JoinRelation")
@Table(schema = "\"OLINGO\"", name = "\"JoinRelation\"")
public class JoinRelation {

  @EmbeddedId
  private JoinRelationKey key;
}
