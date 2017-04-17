package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Clob;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.Lob;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Embeddable
public class DummyEmbeddedToIgnore {

  @Column(name = "\"Name\"", length = 100)
  private String name;

  @Lob
  @Column(name = "\"Large\"")
  @Basic(fetch = FetchType.LAZY)
  private Clob large;
}
