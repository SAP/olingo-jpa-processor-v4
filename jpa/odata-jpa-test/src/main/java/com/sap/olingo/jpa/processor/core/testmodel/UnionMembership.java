package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;

@Entity(name = UnionMembership.ENTITY_TYPE_NAME)
@Table(schema = "\"OLINGO\"", name = "\"Union\"")
@EdmEntityType(visibleFor = @EdmVisibleFor("Company"))
public class UnionMembership {

  public static final String ENTITY_TYPE_NAME = "UnionMembership";

  @Id
  @Column(name = "\"MemberId\"")
  private String memberId;

  @Column(name = "\"UnionName\"")
  private String name;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
  @JoinColumn(name = "\"MemberId\"", referencedColumnName = "\"ID\"", insertable = false, updatable = false,
      nullable = true)
  private Person member;
}