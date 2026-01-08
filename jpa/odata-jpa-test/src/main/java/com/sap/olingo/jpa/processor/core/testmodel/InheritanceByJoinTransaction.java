package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTopLevelElementRepresentation;

@EdmEntityType(as = EdmTopLevelElementRepresentation.AS_ENTITY_TYPE)
@Entity(name = "InheritanceTransaction")
@Table(schema = "\"OLINGO\"", name = "\"InheritanceByJoinTransaction\"")
public class InheritanceByJoinTransaction {

  @Id
  @Column(name = "\"ID\"")
  private String id;

  @Column(name = "\"AccountID\"")
  private String accountId;

  @Column(name = "\"Amount\"", scale = 5, precision = 16)
  private BigDecimal amount;

}
