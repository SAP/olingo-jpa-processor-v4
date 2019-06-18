package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

//Only for Unit Tests
@EdmIgnore
@Embeddable
public class AddressDeepProtected {

  @Column(name = "\"AddressType\"")
  private String type;

  @Embedded
  private InhouseAddressWithProtection inhouseAddress;

}
