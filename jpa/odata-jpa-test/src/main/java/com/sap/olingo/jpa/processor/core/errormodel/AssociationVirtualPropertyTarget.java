package com.sap.olingo.jpa.processor.core.errormodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity(name = "OneToManyVirtualPropertyTarget")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneTarget\"")
public class AssociationVirtualPropertyTarget {

  @Id
  @Column(name = "\"ID\"")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"SOURCE\"", referencedColumnName = "\"ID\"")
  private AssociationVirtualPropertySource sourceVirtual;
}
