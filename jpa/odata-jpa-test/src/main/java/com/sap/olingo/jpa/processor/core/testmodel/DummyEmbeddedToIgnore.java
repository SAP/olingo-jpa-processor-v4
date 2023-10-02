package com.sap.olingo.jpa.processor.core.testmodel;

import java.sql.Blob;
import java.sql.Clob;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@EdmIgnore
@Embeddable
public class DummyEmbeddedToIgnore {

  @Lob
  @Column(name = "\"Command\"")
  @Basic(fetch = FetchType.LAZY)
  private Clob command;

  @Lob
  @Column(name = "\"LargeBytes\"")
  @Basic(fetch = FetchType.LAZY)
  private Blob largeBytes;
}
