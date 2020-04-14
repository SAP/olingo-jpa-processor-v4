package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

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

  public CountryRestriction(InstanceRestrictionKey id) {
    super();
    this.id = id;
  }

  public String getFromCountry() {
    return fromCountry;
  }

  public String getToCountry() {
    return toCountry;
  }

  public void setFromCountry(String fromCountry) {
    this.fromCountry = fromCountry;
  }

  public void setToCountry(String toCountry) {
    this.toCountry = toCountry;
  }
}
