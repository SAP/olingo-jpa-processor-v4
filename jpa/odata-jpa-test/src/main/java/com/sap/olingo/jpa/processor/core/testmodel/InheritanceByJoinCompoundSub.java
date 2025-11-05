package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity(name = "InheritanceByJoinCompoundSub")
// Looks like Hibernate requires that all keys are mentioned. EclipseLink accepted also only two
@PrimaryKeyJoinColumn(name = "\"CodePublisher\"", referencedColumnName = "\"CodePublisher\"")
@PrimaryKeyJoinColumn(name = "\"CodeID\"", referencedColumnName = "\"CodeID\"")
@PrimaryKeyJoinColumn(name = "\"PartCode\"", referencedColumnName = "\"DivisionCode\"")
public class InheritanceByJoinCompoundSub extends InheritanceByJoinCompoundSuper {

  @Column(name = "\"Value\"", length = 31)
  private String value;
}
