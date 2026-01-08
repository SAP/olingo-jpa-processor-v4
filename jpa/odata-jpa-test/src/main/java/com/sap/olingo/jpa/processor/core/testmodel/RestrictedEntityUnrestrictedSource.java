package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "RestrictedEntityUnrestrictedSource")
@Table(schema = "\"OLINGO\"", name = "\"RestrictedEntityUnrestrictedSource\"")
public class RestrictedEntityUnrestrictedSource {
  @Id
  @Column(name = "\"Id\"")
  private UUID id;

  @Embedded
  private RestrictedEntityComplex relation;
}
