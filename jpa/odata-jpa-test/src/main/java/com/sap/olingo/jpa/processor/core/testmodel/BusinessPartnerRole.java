package com.sap.olingo.jpa.processor.core.testmodel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@IdClass(BusinessPartnerRoleKey.class)
//@ReadOnly
@Entity(name = "BusinessPartnerRole")
@Table(schema = "\"OLINGO\"", name = "\"BusinessPartnerRole\"")
public class BusinessPartnerRole {

  @Id
  @Column(name = "\"BusinessPartnerID\"")
  private String businessPartnerID;
  @Id
  @Column(name = "\"BusinessPartnerRole\"")
  private String roleCategory;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "\"BusinessPartnerID\"", insertable = false, updatable = false)
  private BusinessPartner businessPartner;

  @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinColumn(name = "\"BusinessPartnerID\"", insertable = false, updatable = false)
  private Organization organization;

  public BusinessPartnerRole() {
    super();
  }

  public BusinessPartnerRole(final String businessPartnerID, final String roleCategory) {
    super();
    this.setBusinessPartnerID(businessPartnerID);
    this.setRoleCategory(roleCategory);
  }

  public <T extends BusinessPartner> BusinessPartnerRole(final T businessPartner, final String roleCategory) {
    super();
    this.setBusinessPartner(businessPartner);
    this.setRoleCategory(roleCategory);
  }

  public String getBusinessPartnerID() {
    return businessPartnerID;
  }

  public String getRoleCategory() {
    return roleCategory;
  }

  public BusinessPartner getBusinessPartner() {
    return businessPartner;
  }

  public void setBusinessPartnerID(final String businessPartnerID) {
    this.businessPartnerID = businessPartnerID;
  }

  public void setRoleCategory(final String roleCategory) {
    this.roleCategory = roleCategory;
  }

  public <T extends BusinessPartner> void setBusinessPartner(final T businessPartner) {
    businessPartnerID = businessPartner.getID();
    this.businessPartner = businessPartner;

  }

  public Organization getOrganization() {
    return organization;
  }

  public void setOrganization(final Organization organization) {
    this.organization = organization;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((businessPartnerID == null) ? 0 : businessPartnerID.hashCode());
    result = prime * result + ((roleCategory == null) ? 0 : roleCategory.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final BusinessPartnerRole other = (BusinessPartnerRole) obj;
    if (businessPartnerID == null) {
      if (other.businessPartnerID != null) return false;
    } else if (!businessPartnerID.equals(other.businessPartnerID)) return false;
    if (roleCategory == null) {
      if (other.roleCategory != null) return false;
    } else if (!roleCategory.equals(other.roleCategory)) return false;
    return true;
  }
}
