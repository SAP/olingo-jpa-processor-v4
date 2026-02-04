package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "OneToManyVirtualPropertySource")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneSource\"")
public class AssociationVirtualPropertySource {

  @Id
  @Column(name = "\"ID\"")
  protected String key;

  @OneToMany(mappedBy = "sourceVirtual", fetch = FetchType.LAZY)
  private final List<AssociationVirtualPropertyTarget> targets = new ArrayList<>();
}
