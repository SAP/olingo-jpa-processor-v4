package com.sap.olingo.jpa.processor.core.errormodel;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@IdClass(CompoundKey.class)
@Entity(name = "TeamWithTransientKey")
@Table(schema = "\"OLINGO\"", name = "\"Team\"")
public class TeamWithTransientKey {

  @Id
  @Column(name = "\"TeamKey\"")
  private String iD;

  @Id
  @Transient
  @EdmTransient(calculator = DummyPropertyCalculator.class)
  private String name;

  @Override
  public int hashCode() {
    return Objects.hash(iD, name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final TeamWithTransientKey other = (TeamWithTransientKey) obj;
    return Objects.equals(iD, other.iD) && Objects.equals(name, other.name);
  }
}
