package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity(name = "InheritanceAccount")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "\"Type\"")
@Table(schema = "\"OLINGO\"", name = "\"InheritanceByJoinAccount\"")
public class InheritanceByJoinAccount {
  @Id
  @Column(name = "\"ID\"")
  private String accountId;

  @Column(name = "\"Owner\"")
  private String owner;

  @Column(name = "\"Type\"", length = 31)
  private String type;

  @Column(name = "\"Amount\"", scale = 5, precision = 16)
  private BigDecimal amount;

  @OneToMany
  @JoinColumn(name = "\"AccountID\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false,
      nullable = true)
  private List<InheritanceByJoinTransaction> transactions;

}
