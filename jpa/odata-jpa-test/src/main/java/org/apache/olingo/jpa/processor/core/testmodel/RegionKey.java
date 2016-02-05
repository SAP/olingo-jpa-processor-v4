package org.apache.olingo.jpa.processor.core.testmodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;

@Embeddable
public class RegionKey {
  @Id
  @Column(name = "\"CountryISOCode\"")
  private String countryCode;
  @Id
  @Column(name = "\"RegionISOCode\"")
  private String regionCode;
  @Id
  @Column(name = "\"LanguageISO\"")
  private String language;

  public String getCountryCode() {
    return countryCode;
  }

  public String getRegionCode() {
    return regionCode;
  }

  public String getLanguage() {
    return language;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public void setRegionCode(String regionCode) {
    this.regionCode = regionCode;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
}
