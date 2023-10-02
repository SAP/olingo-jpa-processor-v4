package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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
  @JoinColumn(name = "target")
  private AssociationOneToOneTarget columnTarget;
}
