package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Version;

import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssoziation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunctions;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;

@Inheritance
@DiscriminatorColumn(name = "\"Type\"")
@Entity(name = "BusinessPartner")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
@EdmFunctions({
    @EdmFunction(
        name = "CountRoles",
        functionName = "COUNT_ROLES",
        returnType = @EdmFunction.ReturnType(isCollection = true),
        parameter = { @EdmParameter(name = "Amount", parameterName = "a", type = String.class),
        }),

    @EdmFunction(
        name = "max",
        functionName = "MAX",
        isBound = false,
        hasFunctionImport = false,
        returnType = @EdmFunction.ReturnType(type = BigDecimal.class, isCollection = false),
        parameter = { @EdmParameter(name = "Path", parameterName = "path", type = String.class),
        }),

    @EdmFunction(
        name = "IsPrime",
        functionName = "IS_PRIME",
        isBound = false,
        hasFunctionImport = true,
        returnType = @EdmFunction.ReturnType(type = Boolean.class, isNullable = false),
        parameter = { @EdmParameter(name = "Number", type = BigDecimal.class, precision = 32, scale = 0) }),

})

public abstract class BusinessPartner implements KeyAccess {
  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  protected String type;

  @Column(name = "\"CreatedAt\"", precision = 3, insertable = false, updatable = false)
  private Timestamp creationDateTime;

  @EdmIgnore
  @Column(name = "\"CustomString1\"")
  @Convert(converter = StringConverter.class)
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

  @EdmAnnotation(term = "Core.IsLanguageDependent", constantExpression = @EdmAnnotation.ConstantExpression(
      type = ConstantExpressionType.Bool, value = "true"))
  @EdmDescriptionAssoziation(languageAttribute = "key/language", descriptionAttribute = "name",
      valueAssignments = {
          @EdmDescriptionAssoziation.valueAssignment(attribute = "key/codePublisher", value = "ISO"),
          @EdmDescriptionAssoziation.valueAssignment(attribute = "key/codeID", value = "3166-1") })
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "\"DivisionCode\"", referencedColumnName = "\"Country\"")
  private Collection<AdministrativeDivisionDescription> locationName;

  @Embedded
  protected CommunicationData communicationData;

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

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BusinessPartner other = (BusinessPartner) obj;
    if (iD == null) {
      if (other.iD != null)
        return false;
    } else if (!iD.equals(other.iD))
      return false;
    return true;
  }

  public PostalAddressData getAddress() {
    return address;
  }

  public AdministrativeInformation getAdministrativeInformation() {
    return administrativeInformation;
  }

  public CommunicationData getCommunicationData() {
    return communicationData;
  }

  public String getCountry() {
    return country;
  }

  public Timestamp getCreationDateTime() {
    return creationDateTime;
  }

  public BigDecimal getCustomNum1() {
    return customNum1;
  }

  public BigDecimal getCustomNum2() {
    return customNum2;
  }

  public String getCustomString1() {
    return customString1;
  }

  public String getCustomString2() {
    return customString2;
  }

  public long getETag() {
    return eTag;
  }

  public String getID() {
    return iD;
  }

  @Override
  public Object getKey() {
    return iD;
  }

  public Collection<BusinessPartnerRole> getRoles() {
    if (roles == null)
      roles = new ArrayList<>();
    return roles;
  }

  public String getType() {
    return type;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((iD == null) ? 0 : iD.hashCode());
    return result;
  }

  public void setAddress(PostalAddressData address) {
    this.address = address;
  }

  public void setAdministrativeInformation(AdministrativeInformation administrativeInformation) {
    this.administrativeInformation = administrativeInformation;
  }

  public void setCommunicationData(CommunicationData communicationData) {
    this.communicationData = communicationData;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public void setCreationDateTime(Timestamp creationDateTime) {
    this.creationDateTime = creationDateTime;
  }

  public void setCustomNum1(BigDecimal customNum1) {
    this.customNum1 = customNum1;
  }

  public void setCustomNum2(BigDecimal customNum2) {
    this.customNum2 = customNum2;
  }

  public void setCustomString1(String customString1) {
    this.customString1 = customString1;
  }

  public void setCustomString2(String customString2) {
    this.customString2 = customString2;
  }

  public void setETag(long eTag) {
    this.eTag = eTag;
  }

  public void setID(String iD) {
    this.iD = iD;
  }

  public void setRoles(Collection<BusinessPartnerRole> roles) {
    this.roles = roles;
  }

  public void setType(String type) {
    this.type = type;
  }

  @PrePersist
  public void onCreate() {
    administrativeInformation = new AdministrativeInformation();
    long time = new Date().getTime();
    ChangeInformation created = new ChangeInformation("99", new Timestamp(time));
    administrativeInformation.setCreated(created);
    administrativeInformation.setUpdated(created);
  }

  public Collection<AdministrativeDivisionDescription> getLocationName() {
    return locationName;
  }
}
