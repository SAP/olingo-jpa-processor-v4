package com.sap.olingo.jpa.processor.core.testmodel;

import static com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation.AS_SINGLETON;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;

@Table(schema = "\"OLINGO\"", name = "\"GeneralSettings\"")
@Entity(name = "Singleton")
@EdmEntityType(as = AS_SINGLETON)
public class Singleton {

  @Id
  @Column(name = "\"Name\"", length = 255, insertable = true, updatable = false)
  private String name;

  @OneToMany(mappedBy = "generalSettings", fetch = FetchType.LAZY)
  private List<EntityTypeOnly> details;
}
