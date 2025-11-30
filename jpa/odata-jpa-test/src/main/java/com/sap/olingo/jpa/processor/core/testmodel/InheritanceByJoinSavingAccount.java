package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@DiscriminatorValue("SavingAccount")
@Entity(name = "InheritanceSavingAccount")
@Table(schema = "\"OLINGO\"", name = "\"InheritanceByJoinSavingAccount\"")
@PrimaryKeyJoinColumn(name = "\"ID\"")
public class InheritanceByJoinSavingAccount extends InheritanceByJoinAccount {

  @Column(name = "\"InterestRate\"", precision = 5, scale = 2)
  private BigDecimal interestRate;
}
