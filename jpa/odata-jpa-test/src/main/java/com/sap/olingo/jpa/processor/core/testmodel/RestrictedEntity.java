package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;

@Entity(name = RestrictedEntity.ENTITY_TYPE_NAME)
@Table(schema = "\"OLINGO\"", name = "\"RestrictedEntity\"")
@EdmEntityType(as = EdmTopLevelElementRepresentation.AS_ENTITY_TYPE, visibleFor = @EdmVisibleFor("Person"))
public class RestrictedEntity {
  public static final String ENTITY_TYPE_NAME = "RestrictedEntity";

  @Id
  @Column(name = "\"Id\"")
  private UUID id;

  @Column(name = "\"ParentId\"")
  private UUID parentId;
}
