package com.sap.olingo.jpa.processor.core.testmodel;

import java.time.LocalDate;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Id;

public class TemporalWithValidityPeriodKey {

  @Id
  @Column(name = "\"ID\"", length = 32)
  private String id;

  @Id
  @Column(name = "\"StartDate\"")
  private LocalDate validityStartDate;

  public TemporalWithValidityPeriodKey(final String id, final LocalDate validityStartDate) {
    super();
    this.setId(id);
    this.setValidityStartDate(validityStartDate);
  }

  public TemporalWithValidityPeriodKey() {
    // Needed for JPA
  }

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

  @Override
  public int hashCode() {
    return Objects.hash(id, validityStartDate);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof TemporalWithValidityPeriodKey)) return false;
    final TemporalWithValidityPeriodKey other = (TemporalWithValidityPeriodKey) obj;
    return Objects.equals(id, other.id) && Objects.equals(validityStartDate, other.validityStartDate);
  }

}
