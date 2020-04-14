package com.sap.olingo.jpa.processor.core.errormodel;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@Entity(name = "TeamWithTransientEmbeddableKey")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class TeamWithTransientEmbeddableKey {

  @EmbeddedId
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private CompoundKey key;

}
