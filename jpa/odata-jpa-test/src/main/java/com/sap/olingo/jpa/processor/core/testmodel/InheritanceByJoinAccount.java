package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity(name = "InheritanceAccount")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "\"Type\"")
@Table(schema = "\"OLINGO\"", name = "\"InheritanceByJoinAccount\"")
public class InheritanceByJoinAccount {
  @Id
  @Column(name = "\"ID\"")
  private String accountId;

  @Column(name = "\"Type\"", length = 31)
  private String type;
}
