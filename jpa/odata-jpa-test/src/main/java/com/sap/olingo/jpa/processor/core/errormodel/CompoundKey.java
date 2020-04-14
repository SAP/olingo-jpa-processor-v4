package com.sap.olingo.jpa.processor.core.errormodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CompoundKey implements Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = -2350388598203342905L;

  @Column(name = "\"TeamKey\"")
  private String iD;

  @Column(name = "\"Name\"")
  private String name;

  public CompoundKey() {
    // Needed for JPA
  }

  public CompoundKey(final String iD, final String name) {
    super();
    this.iD = iD;
    this.name = name;
  }
}
