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

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public InhouseAddressWithProtection getInhouseAddress() {
    return inhouseAddress;
  }

  public void setInhouseAddress(final InhouseAddressWithProtection inhouseAddress) {
    this.inhouseAddress = inhouseAddress;
  }

}
