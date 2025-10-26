package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@IdClass(AdministrativeDivisionKey.class)
@DiscriminatorColumn(name = "\"Type\"")
@Entity(name = "InheritanceByJoinCompoundSuper")
@Inheritance(strategy = InheritanceType.JOINED)
public class InheritanceByJoinCompoundSuper {
  @Id
  @Column(name = "\"CodePublisher\"", length = 10)
  private String codePublisher;
  @Id
  @Column(name = "\"CodeID\"", length = 10)
  private String codeID;
  @Id
  @Column(name = "\"DivisionCode\"", length = 10)
  private String divisionCode;

  @Column(name = "\"Type\"", length = 31)
  private String type;
}
