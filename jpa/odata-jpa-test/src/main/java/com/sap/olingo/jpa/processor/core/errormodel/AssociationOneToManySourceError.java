package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "OneToManySource")
@Table(schema = "\"OLINGO\"", name = "\"AssociationOneToOneSource\"")
public class AssociationOneToManySourceError {

  @Id
  @Column(name = "key")
  protected String key;

  @OneToMany(fetch = FetchType.LAZY)
  private List<AssociationOneToManyTargetError> noMappedTarget;
}
