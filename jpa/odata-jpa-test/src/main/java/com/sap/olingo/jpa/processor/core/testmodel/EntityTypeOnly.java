package com.sap.olingo.jpa.processor.core.testmodel;

import static com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation.AS_ENTITY_TYPE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

@Table(schema = "\"OLINGO\"", name = "\"DetailSettings\"")
@Entity(name = "AsEntityType")
@EdmEntityType(as = AS_ENTITY_TYPE)
public class EntityTypeOnly {

  @Id
  @Column(name = "\"Id\"")
  private Integer id;

  @Column(name = "\"Name\"", length = 255)
  private String name;

  @EdmIgnore
  @Column(name = "\"GeneralName\"", length = 255, insertable = true, updatable = false)
  private String generalName;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "\"GeneralName\"", insertable = false, updatable = false)
  private Singleton generalSettings;
}
