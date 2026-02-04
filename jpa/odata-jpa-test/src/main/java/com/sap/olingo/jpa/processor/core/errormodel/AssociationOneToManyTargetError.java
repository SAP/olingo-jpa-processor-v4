package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "OneToManyTarget")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToManyTarget\"")
public class AssociationOneToManyTargetError {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Column(name = "source_key")
  protected String sourceKey;

  @ManyToOne(fetch = FetchType.LAZY)
  private AssociationOneToManySourceError defaultSource;
}
