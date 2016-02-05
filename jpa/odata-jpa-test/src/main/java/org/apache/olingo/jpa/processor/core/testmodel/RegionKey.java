package org.apache.olingo.jpa.processor.core.testmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;



public class RegionKey implements Serializable{
  /**
   * 
   */
  private static final long serialVersionUID = 4843041820527005995L;
  
  @Id
  @Column(name = "\"CountryISOCode\"")
  private String countryCode;
  @Column(name = "\"RegionISOCode\"")
  @Id
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((countryCode == null) ? 0 : countryCode.hashCode());
    result = prime * result + ((language == null) ? 0 : language.hashCode());
    result = prime * result + ((regionCode == null) ? 0 : regionCode.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    RegionKey other = (RegionKey) obj;
    if (countryCode == null) {
      if (other.countryCode != null) return false;
    } else if (!countryCode.equals(other.countryCode)) return false;
    if (language == null) {
      if (other.language != null) return false;
    } else if (!language.equals(other.language)) return false;
    if (regionCode == null) {
      if (other.regionCode != null) return false;
    } else if (!regionCode.equals(other.regionCode)) return false;
    return true;
  }
}
