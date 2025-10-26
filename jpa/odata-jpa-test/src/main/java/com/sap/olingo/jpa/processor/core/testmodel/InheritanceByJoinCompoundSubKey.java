package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class InheritanceByJoinCompoundSubKey {

  @Id
  @Column(name = "\"CodePublisher\"", length = 10)
  private String codePublisher;

  @Id
  @Column(name = "\"CodeID\"", length = 10)
  private String codeID;

  @Id
  @Column(name = "\"PartCode\"", length = 10)
  private String partCode;
}
