package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;

//Only for Unit Tests
@EdmIgnore
@Embeddable
public class AddressDeepThreeProtections {

  @Column(name = "\"AddressType\"")
  private String type;

  @Embedded
  private InhouseAddressWithThreeProtections inhouseAddress;

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public InhouseAddressWithThreeProtections getInhouseAddress() {
    return inhouseAddress;
  }

  public void setInhouseAddress(final InhouseAddressWithThreeProtections inhouseAddress) {
    this.inhouseAddress = inhouseAddress;
  }

}
