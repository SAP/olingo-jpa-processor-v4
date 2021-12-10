package com.sap.olingo.jpa.processor.core.errormodel;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class CompoundKey implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -2350388598203342905L;

  @Column(name = "\"TeamKey\"")
  private String iD;

  @Column(name = "\"Name\"")
  private String name;

  public CompoundKey() {
    // Needed for JPA
  }

  public CompoundKey(final String iD, final String name) {
    super();
    this.iD = iD;
    this.name = name;
  }

  public String getID() {
    return iD;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public static long getSerialversionuid() {
    return serialVersionUID;
  }

  @Override
  public int hashCode() {
    return Objects.hash(iD, name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final CompoundKey other = (CompoundKey) obj;
    return Objects.equals(iD, other.iD) && Objects.equals(name, other.name);
  }
}
