package com.sap.olingo.jpa.processor.core.testmodel;

import java.util.Collection;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssociation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmVisibleFor;

@Embeddable
public class PostalAddressDataWithGroup {
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
  @EdmVisibleFor("Company")
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

  public void setStreetName(final String streetName) {
    this.streetName = streetName;
  }

  public void setHouseNumber(final String houseNumber) {
    this.houseNumber = houseNumber;
  }

  public void setPOBox(final String pOBox) {
    this.pOBox = pOBox;
  }

  public void setCityName(final String cityName) {
    this.cityName = cityName;
  }

  public void setPostalCode(final String postalCode) {
    this.postalCode = postalCode;
  }

  public void setRegion(final String region) {
    this.region = region;
  }

  public String getCountry() {
    return country;
  }

  @Override
  public String toString() {
    return "PostalAddressData [streetName=" + streetName + ", houseNumber=" + houseNumber + ", pOBox=" + pOBox
        + ", postalCode=" + postalCode + ", cityName=" + cityName + ", country=" + country + ", region=" + region + "]";
  }

  public String getRegionCodePublisher() {
    return regionCodePublisher;
  }

  public void setRegionCodePublisher(final String regionCodePublisher) {
    this.regionCodePublisher = regionCodePublisher;
  }

  public String getRegionCodeID() {
    return regionCodeID;
  }

  public void setRegionCodeID(final String regionCodeID) {
    this.regionCodeID = regionCodeID;
  }
}
