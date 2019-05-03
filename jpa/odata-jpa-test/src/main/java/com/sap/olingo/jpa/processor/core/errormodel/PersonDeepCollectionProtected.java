package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Version;

import com.sap.olingo.jpa.processor.core.testmodel.AddressDeepProtected;

@Entity(name = "PersonDeepProtected")

public class PersonDeepCollectionProtected {// #NOSONAR use equal method from
  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag; // BusinessPartner

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  protected String type;

  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(schema = "\"OLINGO\"", name = "\"InhouseAddress\"",
      joinColumns = @JoinColumn(name = "\"ID\""))
  private List<AddressDeepProtected> inhouseAddress;

  public PersonDeepCollectionProtected() {
    type = "1";
  }

}
