package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@DiscriminatorValue("CurrentAccount")
@Entity(name = "InheritanceCurrentAccount")
@Table(schema = "\"OLINGO\"", name = "\"InheritanceByJoinCurrentAccount\"")
public class InheritanceByJoinCurrentAccount extends InheritanceByJoinAccount {

  @Column(name = "\"InterestRate\"", precision = 5, scale = 2)
  private BigDecimal interestRate;

  @Column(name = "\"BorrowingRate\"", precision = 5, scale = 2)
  private BigDecimal borrowingRate;

  @Column(name = "\"CreditLimit\"", precision = 10, scale = 2)
  private BigDecimal creditLimit;

}
