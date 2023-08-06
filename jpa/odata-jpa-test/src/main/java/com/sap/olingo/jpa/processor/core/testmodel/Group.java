package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@MappedSuperclass
public class Group extends AbstractGroup {

  @EdmIgnore
  @Column(name = "\"GroupLead\"")
  private String leadId;

  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST, optional = true)
  @JoinColumn(referencedColumnName = "\"ID\"", name = "\"GroupLead\"", nullable = false,
      insertable = false, updatable = false)
  private Person lead;

  @EdmTransient(requiredAttributes = { "id", "name" }, calculator = GroupNameCalculator.class)
  @Transient
  private String fullName;

  @Override
  public boolean equals(final Object object) {
    return super.equals(object);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
