package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(schema = "\"OLINGO\"", name = "\"CountryRestriction\"")
public class CountryRestriction {

  @EmbeddedId
  private InstanceRestrictionKey id;

  @Column(name = "\"From\"", length = 4)
  private String fromCountry;

  @Column(name = "\"To\"", length = 4)
  private String toCountry;

  public CountryRestriction() {
    // Needed for JPA
  }

  public CountryRestriction(final InstanceRestrictionKey id) {
    super();
    this.id = id;
  }

  public String getFromCountry() {
    return fromCountry;
  }

  public String getToCountry() {
    return toCountry;
  }

  public void setFromCountry(final String fromCountry) {
    this.fromCountry = fromCountry;
  }

  public void setToCountry(final String toCountry) {
    this.toCountry = toCountry;
  }
}
