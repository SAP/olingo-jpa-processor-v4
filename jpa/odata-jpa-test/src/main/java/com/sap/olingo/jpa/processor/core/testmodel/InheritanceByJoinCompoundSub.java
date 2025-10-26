package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@DiscriminatorValue("Sub")
@Entity(name = "InheritanceByJoinCompoundSub")
@PrimaryKeyJoinColumn(name = "\"CodeID\"", referencedColumnName = "\"CodeID\"")
@PrimaryKeyJoinColumn(name = "\"PartCode\"", referencedColumnName = "\"DivisionCode\"")
public class InheritanceByJoinCompoundSub extends InheritanceByJoinCompoundSuper {

  @Column(name = "\"Value\"", length = 31)
  private String value;
}
