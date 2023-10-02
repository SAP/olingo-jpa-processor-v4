package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

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
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof TemporalWithValidityPeriod)) return false;
    final TemporalWithValidityPeriod other = (TemporalWithValidityPeriod) obj;
    return Objects.equals(id, other.id) && Objects.equals(validityStartDate, other.validityStartDate);
  }

}
