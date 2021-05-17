package com.sap.olingo.jpa.processor.core.errormodel;

import static com.sap.olingo.jpa.metadata.core.edm.annotation.TopLevelElementRepresentation.AS_SINGLETON;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAsEntitySet;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;

@Table(schema = "\"OLINGO\"", name = "\"GeneralSettings\"")
@Entity(name = "Singleton")
@EdmEntityType(as = AS_SINGLETON)
@EdmAsEntitySet
public class SingletonAsEntitySet {
  @Id
  @Column(name = "\"Name\"", length = 255, insertable = true, updatable = false)
  private String name;
}
