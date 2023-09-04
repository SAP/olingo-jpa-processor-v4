package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "AssociationOneToOneTarget")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneTarget\"")
public class AssociationOneToOneTarget {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @OneToOne(mappedBy = "defaultTarget", fetch = FetchType.LAZY)
  private AssociationOneToOneSource defaultSource;

}
