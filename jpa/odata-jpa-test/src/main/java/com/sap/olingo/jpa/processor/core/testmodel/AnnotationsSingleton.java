package com.sap.olingo.jpa.processor.core.testmodel;

import static com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation.AS_SINGLETON;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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
