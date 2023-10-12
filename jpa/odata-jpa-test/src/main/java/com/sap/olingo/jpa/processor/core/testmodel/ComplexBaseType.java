package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ComplexBaseType {

  @Column
  private String oneAttribute;
}
