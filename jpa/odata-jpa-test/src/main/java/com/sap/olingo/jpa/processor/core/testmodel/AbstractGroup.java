package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractGroup {

  @Id
  @Column(name = "\"TeamKey\"")
  private String id;

  @Column(name = "\"Name\"")
  private String name;

  protected AbstractGroup() {
    super();
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(final Object object) {
    if (this == object) return true;
    if (!(object instanceof AbstractGroup)) return false;
    final AbstractGroup other = (AbstractGroup) object;
    return Objects.equals(id, other.id);
  }

}