package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@DiscriminatorValue("LockedSavingAccount")
@Entity(name = "InheritanceLockedSavingAccount")
@Table(schema = "\"OLINGO\"", name = "\"InheritanceByJoinLockedSavingAccount\"")
@PrimaryKeyJoinColumn(name = "\"AccountId\"")
public class InheritanceByJoinLockedSavingAccount extends InheritanceByJoinSavingAccount {

  @Column(name = "\"LockingPeriod\"")
  private int lockingPeriod;
}
