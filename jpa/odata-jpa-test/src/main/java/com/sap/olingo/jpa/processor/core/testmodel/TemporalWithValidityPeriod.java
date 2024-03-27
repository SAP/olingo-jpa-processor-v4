package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity()
@IdClass(TemporalWithValidityPeriodKey.class)
@Table(schema = "\"OLINGO\"", name = "\"TemporalWithValidityPeriod\"")
public class TemporalWithValidityPeriod {

  @Id
  @Column(name = "\"ID\"", length = 32)
  private String id;

  @Id
  @Column(name = "\"StartDate\"")
  private LocalDate validityStartDate;

  @Column(name = "\"EndDate\"")
  private LocalDate validityEndDate;

  @Column(name = "\"Value\"", length = 255)
  private String value;

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public LocalDate getValidityStartDate() {
    return validityStartDate;
  }

  public void setValidityStartDate(final LocalDate validityStartDate) {
    this.validityStartDate = validityStartDate;
  }

  public LocalDate getValidityEndDate() {
    return validityEndDate;
  }

  public void setValidityEndDate(final LocalDate validityEndDate) {
    this.validityEndDate = validityEndDate;
  }

  public String getValue() {
    return value;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, validityStartDate);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof final TemporalWithValidityPeriod other)
      return Objects.equals(id, other.id) && Objects.equals(validityStartDate, other.validityStartDate);
    return false;
  }

}
