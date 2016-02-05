package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity
@IdClass(RegionKey.class)
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::Region\"")
public class Region {
//  @EmbeddedId
//  private RegionKey regionKey;
  @Id
  @Column(name = "\"CountryISOCode\"")
  private String countryCode;
  @Id
  @Column(name = "\"RegionISOCode\"")
  private String regionCode;
  @Id
  @Column(name = "\"LanguageISO\"")
  private String language;

  @Column(name = "\"Name\"", length = 100)
  private String name;

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getRegionCode() {
    return regionCode;
  }

  public void setRegionCode(String regionCode) {
    this.regionCode = regionCode;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
