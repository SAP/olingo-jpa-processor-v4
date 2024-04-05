package com.sap.olingo.jpa.processor.core.testmodel;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import jakarta.persistence.AssociationOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import org.apache.olingo.commons.api.edm.provider.annotation.CsdlConstantExpression.ConstantExpressionType;

import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmAnnotation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmDescriptionAssociation;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmEntityType;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmFunction;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmIgnore;
import com.sap.olingo.jpa.metadata.core.edm.annotation.EdmParameter;

@Inheritance
@DiscriminatorColumn(name = "\"Type\"")
@Entity(name = "BusinessPartner")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartner\"")
@EdmEntityType(extensionProvider = EmptyQueryExtensionProvider.class)

@EdmFunction(
    name = "CountRoles",
    functionName = "COUNT_ROLES",
    returnType = @EdmFunction.ReturnType(isCollection = true),
    parameter = { @EdmParameter(name = "Amount", parameterName = "a", type = String.class),
    })

@EdmFunction(
    name = "max",
    functionName = "MAX",
    isBound = false,
    hasFunctionImport = false,
    returnType = @EdmFunction.ReturnType(type = BigDecimal.class, isCollection = false),
    parameter = { @EdmParameter(name = "Path", parameterName = "path", type = String.class),
    })

@EdmFunction(
    name = "IsPrime",
    functionName = "IS_PRIME",
    isBound = false,
    hasFunctionImport = true,
    returnType = @EdmFunction.ReturnType(type = Boolean.class, isNullable = false),
    parameter = { @EdmParameter(name = "Number", type = BigDecimal.class, precision = 32, scale = 0) })

public abstract class BusinessPartner implements KeyAccess {
  @Id
  @Column(name = "\"ID\"")
  protected String iD;

  @Version
  @Column(name = "\"ETag\"", nullable = false)
  protected long eTag;

  @Column(name = "\"Type\"", length = 1, insertable = false, updatable = false, nullable = false)
  protected String type;

  @Column(name = "\"CreatedAt\"", precision = 2, insertable = false, updatable = false)
  @Convert(converter = DateTimeConverter.class)
  private LocalDateTime creationDateTime;

  @EdmIgnore
  @Column(name = "\"CustomString1\"")
  @Convert(converter = StringConverter.class)
  private String customString1;

  @EdmIgnore
  @Column(name = "\"CustomString2\"")
  private String customString2;

  @EdmIgnore
  @Column(name = "\"CustomNum1\"", precision = 16, scale = 5)
  private BigDecimal customNum1;

  @EdmIgnore
  @Column(name = "\"CustomNum2\"", precision = 34)
  private BigDecimal customNum2;

  @Column(name = "\"Country\"", length = 4)
  private String country;

  @EdmAnnotation(term = "Core.IsLanguageDependent", constantExpression = @EdmAnnotation.ConstantExpression(
      type = ConstantExpressionType.Bool, value = "true"))
  @EdmDescriptionAssociation(languageAttribute = "key/language", descriptionAttribute = "name",
      valueAssignments = {
          @EdmDescriptionAssociation.valueAssignment(attribute = "key/codePublisher", value = "ISO"),
          @EdmDescriptionAssociation.valueAssignment(attribute = "key/codeID", value = "3166-1") })
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "\"DivisionCode\"", referencedColumnName = "\"Country\"")
  private Collection<AdministrativeDivisionDescription> locationName;

  @Embedded
  protected CommunicationData communicationData;

  @Embedded
  @AssociationOverride(name = "countryName",
      joinColumns = @JoinColumn(referencedColumnName = "\"Address.Country\"", name = "\"ISOCode\""))
  @AssociationOverride(name = "regionName",
      joinColumns = {
          @JoinColumn(referencedColumnName = "\"Address.RegionCodePublisher\"", name = "\"CodePublisher\""),
          @JoinColumn(referencedColumnName = "\"Address.RegionCodeID\"", name = "\"CodeID\""),
          @JoinColumn(referencedColumnName = "\"Address.Region\"", name = "\"DivisionCode\"") })
  private PostalAddressData address = new PostalAddressData();

  @Embedded
  private AdministrativeInformation administrativeInformation = new AdministrativeInformation();

  @OneToMany(mappedBy = "businessPartner", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Collection<BusinessPartnerRole> roles;

  public BusinessPartner() { // NOSONAR
    super();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final BusinessPartner other = (BusinessPartner) obj;
    if (iD == null) {
      if (other.iD != null)
        return false;
    } else if (!iD.equals(other.iD)) {
      return false;
    }
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

  public LocalDateTime getCreationDateTime() {
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

  public void setAddress(final PostalAddressData address) {
    this.address = address;
  }

  public void setAdministrativeInformation(final AdministrativeInformation administrativeInformation) {
    this.administrativeInformation = administrativeInformation;
  }

  public void setCommunicationData(final CommunicationData communicationData) {
    this.communicationData = communicationData;
  }

  public void setCountry(final String country) {
    this.country = country;
  }

  public void setCreationDateTime(final LocalDateTime creationDateTime) {
    this.creationDateTime = creationDateTime;
  }

  public void setCustomNum1(final BigDecimal customNum1) {
    this.customNum1 = customNum1;
  }

  public void setCustomNum2(final BigDecimal customNum2) {
    this.customNum2 = customNum2;
  }

  public void setCustomString1(final String customString1) {
    this.customString1 = customString1;
  }

  public void setCustomString2(final String customString2) {
    this.customString2 = customString2;
  }

  public void setETag(final long eTag) {
    this.eTag = eTag;
  }

  public void setID(final String iD) {
    this.iD = iD;
  }

  public void setRoles(final Collection<BusinessPartnerRole> roles) {
    this.roles = roles;
  }

  public void setType(final String type) {
    this.type = type;
  }

  @PrePersist
  public void onCreate() {
    administrativeInformation = new AdministrativeInformation();
    final ChangeInformation created = new ChangeInformation("99", Date.valueOf(LocalDate.now()));
    administrativeInformation.setCreated(created);
    administrativeInformation.setUpdated(created);
  }

  public Collection<AdministrativeDivisionDescription> getLocationName() {
    return locationName;
  }

  public void setLocationName(final Collection<AdministrativeDivisionDescription> locationName) {
    this.locationName = locationName;
  }
}
