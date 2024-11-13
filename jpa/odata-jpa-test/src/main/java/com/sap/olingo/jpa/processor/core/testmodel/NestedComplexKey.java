package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public class NestedComplexKey {

  @Id
  @Column(name = "\"ID\"")
  private String iD;

  @Id
  @Column(name = "\"Number\"")
  private Long number;

  @Override
  public int hashCode() {
    return Objects.hash(iD, number);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof final NestedComplexKey other)
      return Objects.equals(iD, other.iD) && Objects.equals(number, other.number);
    return false;
  }
}
