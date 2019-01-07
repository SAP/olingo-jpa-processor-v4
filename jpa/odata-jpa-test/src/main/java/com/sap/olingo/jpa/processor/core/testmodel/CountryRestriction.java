package com.sap.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(InstanceRestrictionKey.class)
@Table(schema = "\"OLINGO\"", name = "\"CountryRestriction\"")
public class CountryRestriction {

  public CountryRestriction() {
    // Needed for JPA
  }

  public CountryRestriction(InstanceRestrictionKey id) {
    super();
    this.id = id;
  }

  @Id
  private InstanceRestrictionKey id;

  @Column(name = "\"From\"", length = 4)
  private String fromCountry;

  @Column(name = "\"To\"", length = 4)
  private String toCountry;

  public String getFromCountry() {
    return fromCountry;
  }

  public void setFromCountry(String fromCountry) {
    this.fromCountry = fromCountry;
  }

  public String getToCountry() {
    return toCountry;
  }

  public void setToCountry(String toCountry) {
    this.toCountry = toCountry;
  }
}
