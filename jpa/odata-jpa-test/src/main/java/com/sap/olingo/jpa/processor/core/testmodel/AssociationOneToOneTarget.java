package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "AssociationOneToOneTarget")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneTarget\"")
public class AssociationOneToOneTarget {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Column(name = "\"SOURCE\"")
  protected String source;

  @OneToOne(mappedBy = "defaultTarget", fetch = FetchType.LAZY)
  private AssociationOneToOneSource defaultSource;

}
