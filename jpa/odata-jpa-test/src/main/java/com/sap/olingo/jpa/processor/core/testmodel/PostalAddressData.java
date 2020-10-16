package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssociation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmTransient;

@Embeddable
public class PostalAddressData {
  @Column(name = "\"Address.StreetName\"", nullable = true)
  private String streetName;
  @Column(name = "\"Address.StreetNumber\"", nullable = true)
  private String houseNumber;
  @Column(name = "\"Address.PostOfficeBox\"", nullable = true)
  private String pOBox;
  @Column(name = "\"Address.PostalCode\"")
  private String postalCode;
  @Column(name = "\"Address.City\"")
  private String cityName;
  @Column(name = "\"Address.Country\"")
  private String country;
  @EdmIgnore
  @Column(name = "\"Address.RegionCodePublisher\"", length = 10)
  private String regionCodePublisher = "ISO";
  @EdmIgnore
  @Column(name = "\"Address.RegionCodeID\"", length = 10)
  private String regionCodeID = "3166-2";
  @Column(name = "\"Address.Region\"")
  private String region;
  @Transient
  @EdmTransient(calculator = StreetPropertyCalculator.class, requiredAttributes = { "streetName", "houseNumber" })
  private String street;

  @EdmDescriptionAssociation(languageAttribute = "language", descriptionAttribute = "name")
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"ISOCode\"", referencedColumnName = "\"Address.Country\"", insertable = false, updatable = false)
  private Collection<Country> countryName;

  @EdmDescriptionAssociation(languageAttribute = "key/language", descriptionAttribute = "name")
  @OneToMany(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"CodePublisher\"", referencedColumnName = "\"Address.RegionCodePublisher\"",
      insertable = false, updatable = false)
  @JoinColumn(name = "\"CodeID\"", referencedColumnName = "\"Address.RegionCodeID\"", insertable = false,
      updatable = false)
  @JoinColumn(name = "\"DivisionCode\"", referencedColumnName = "\"Address.Region\"", insertable = false,
      updatable = false)
  private Collection<AdministrativeDivisionDescription> regionName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "\"Address.RegionCodePublisher\"", referencedColumnName = "\"CodePublisher\"",
      nullable = false, insertable = false, updatable = false)
  @JoinColumn(name = "\"Address.RegionCodeID\"", referencedColumnName = "\"CodeID\"", nullable = false,
      insertable = false, updatable = false)
  @JoinColumn(name = "\"Address.Region\"", referencedColumnName = "\"DivisionCode\"", nullable = false,
      insertable = false, updatable = false)
  private AdministrativeDivision administrativeDivision;

  public String getStreetName() {
    return streetName;
  }

  public String getHouseNumber() {
    return houseNumber;
  }

  public String getPOBox() {
    return pOBox;
  }

  public String getCityName() {
    return cityName;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public String getRegion() {
    return region;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  public void setHouseNumber(String houseNumber) {
    this.houseNumber = houseNumber;
  }

  public void setPOBox(String pOBox) {
    this.pOBox = pOBox;
  }

  public void setCityName(String cityName) {
    this.cityName = cityName;
  }

  public void setPostalCode(String postalCode) {
    this.postalCode = postalCode;
  }

  public void setRegion(String region) {
    this.region = region;
  }

  public String getCountry() {
    return country;
  }

  public Collection<Country> getCountryName() {
    return countryName;
  }

  public Collection<AdministrativeDivisionDescription> getRegionName() {
    return regionName;
  }

  @Override
  public String toString() {
    return "PostalAddressData [streetName=" + streetName + ", houseNumber=" + houseNumber + ", pOBox=" + pOBox
        + ", postalCode=" + postalCode + ", cityName=" + cityName + ", country=" + country + ", region=" + region + "]";
  }
}
