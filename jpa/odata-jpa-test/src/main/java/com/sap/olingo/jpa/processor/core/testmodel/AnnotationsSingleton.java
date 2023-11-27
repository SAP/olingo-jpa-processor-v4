package com.sap.olingo.jpa.processor.core.testmodel;

import static com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation.AS_SINGLETON;

import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.odata.v4.capabilities.terms.CountRestrictions;

@CountRestrictions(nonCountableProperties = { "details" })
@Table(schema = "\"OLINGO\"", name = "\"GeneralSettings\"")
@Entity(name = "AnnotatedSingleton")
@EdmEntityType(as = AS_SINGLETON)
public class AnnotationsSingleton {

  @Id
  @Column(name = "\"Name\"", length = 255, insertable = true, updatable = false)
  private String name;

  @OneToMany(mappedBy = "generalSettings", fetch = FetchType.LAZY)
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"DetailSettings\"",
      joinColumns = @JoinColumn(name = "\"GeneralName\"", referencedColumnName = "\"Name\""))
  private List<DetailSettings> details;
}
