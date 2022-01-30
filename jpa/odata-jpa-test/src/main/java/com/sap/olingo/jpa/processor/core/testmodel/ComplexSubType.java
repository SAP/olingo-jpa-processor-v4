package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ComplexSubType extends ComplexBaseType {

  @Column
  private Integer num;
}
