package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity(name = "AssociationOneToOneSource")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneSource\"")
public class AssociationOneToOneSource {

  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  /**
   * Association using default column name, which are <target class>_<target key> for the source/owner and target key.
   * So it is expected that table AssociationOneToOneSource contains a column ASSOCIATIONONETOONETARGET_ID.
   */
  @OneToOne(fetch = FetchType.LAZY)
  private AssociationOneToOneTarget defaultTarget;

  /**
   * Association with a given name. So it is expected that table AssociationOneToOneSource contains a column TARGET.
   */
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"TARGET\"")
  private AssociationOneToOneTarget columnTarget;
}
