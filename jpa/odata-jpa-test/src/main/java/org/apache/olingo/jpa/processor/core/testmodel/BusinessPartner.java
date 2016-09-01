package org.apache.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Collection;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssozation;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctionParameter;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import org.apache.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;;

@Inheritance
@DiscriminatorColumn(name = "\"Type\"")
@Entity(name = "BusinessPartner")
@Table(schema = "\"OLINGO\"", name = "\"org.apache.olingo.jpa::BusinessPartner\"")
@EdmFunctions({
    @EdmFunction(
        name = "CountRoles",
        functionName = "COUNT_ROLES",
        returnType = @EdmFunction.ReturnType(isCollection = true),
        parameter = { @EdmFunctionParameter(name = "Amount", parameterName = "a", type = Integer.class),
        }),

    @EdmFunction(
        name = "max",
        functionName = "MAX",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(type = BigDecimal.class, isCollection = false),
        parameter = { @EdmFunctionParameter(name = "Path", parameterName = "path", type = String.class),
        }),

    @EdmFunction(
        name = "IsPrime",
        functionName = "IS_PRIME",
        isBound = false,
        hasFunctionImport = true,
        returnType = @EdmFunction.ReturnType(type = Boolean.class, isNullable = false),
        parameter = { @EdmFunctionParameter(name = "Number", type = BigDecimal.class, precision = 32, scale = 0) }),

})

public abstract class BusinessPartner {
  @Id
  @Column(name = "\"ID\"")
  protected String ID;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Column(name = "\"Type\"", length = 1, nullable = false)
  protected String type;

  @Column(name = "\"CreatedAt\"", precision = 3)
  private Timestamp creationDateTime;

  @EdmIgnore
  @Column(name = "\"CustomString1\"")
  protected String customString1;
  @EdmIgnore
  @Column(name = "\"CustomString2\"")
  protected String customString2;
  @EdmIgnore
  @Column(name = "\"CustomNum1\"", precision = 16, scale = 5)
  protected BigDecimal customNum1;
  @EdmIgnore
  @Column(name = "\"CustomNum2\"", precision = 34)
  protected BigDecimal customNum2;

  @Column(name = "\"Country\"", length = 4)
  private String country;

  @EdmDescriptionAssozation(languageAttribute = "language", descriptionAttribute = "name")
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "\"ISOCode\"", referencedColumnName = "\"Country\"")
  private Collection<Country> locationName;

  @Embedded
  protected CommunicationData communicationData = new CommunicationData();

  @Embedded
  @AssociationOverrides({
      @AssociationOverride(name = "countryName",
          joinColumns = @JoinColumn(referencedColumnName = "\"Address.Country\"", name = "\"ISOCode\"")),
      @AssociationOverride(name = "regionName",
          joinColumns = {
              @JoinColumn(referencedColumnName = "\"Address.RegionCodePublisher\"", name = "\"CodePublisher\""),
              @JoinColumn(referencedColumnName = "\"Address.RegionCodeID\"", name = "\"CodeID\""),
              @JoinColumn(referencedColumnName = "\"Address.Region\"", name = "\"DivisionCode\"") })
  })
  private PostalAddressData address = new PostalAddressData();

  @Embedded
  private AdministrativeInformation administrativeInformation = new AdministrativeInformation();

  @OneToMany(mappedBy = "businessPartner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Collection<BusinessPartnerRole> roles;

  public void setID(String iD) {
    ID = iD;
  }

  public void seteTag(long eTag) {
    this.eTag = eTag;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setCreationDateTime(Timestamp creationDateTime) {
    this.creationDateTime = creationDateTime;
  }

  public void setCustomString1(String customString1) {
    this.customString1 = customString1;
  }

  public void setCustomString2(String customString2) {
    this.customString2 = customString2;
  }

  public void setCustomNum1(BigDecimal customNum1) {
    this.customNum1 = customNum1;
  }

  public void setCustomNum2(BigDecimal customNum2) {
    this.customNum2 = customNum2;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setLocationName(Collection<Country> locationName) {
    this.locationName = locationName;
  }

  public void setCommunicationData(CommunicationData communicationData) {
    this.communicationData = communicationData;
  }

  public void setAddress(PostalAddressData address) {
    this.address = address;
  }

  public void setAdministrativeInformation(AdministrativeInformation administrativeInformation) {
    this.administrativeInformation = administrativeInformation;
  }

  public void setRoles(Collection<BusinessPartnerRole> roles) {
    this.roles = roles;
  }
}
