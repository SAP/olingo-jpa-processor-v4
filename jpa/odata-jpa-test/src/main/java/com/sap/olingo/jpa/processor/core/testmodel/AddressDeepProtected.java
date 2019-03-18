package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

//Only for Unit Tests
@Embeddable
public class AddressDeepProtected {

  @Column(name = "\"AddressType\"")
  private String type;

  @Embedded
  private InhouseAddressWithProtection inhouseAddress;

}
